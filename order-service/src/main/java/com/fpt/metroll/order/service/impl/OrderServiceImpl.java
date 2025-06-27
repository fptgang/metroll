package com.fpt.metroll.order.service.impl;

import com.fpt.metroll.order.document.Order;
import com.fpt.metroll.order.document.OrderDetail;
import com.fpt.metroll.order.domain.mapper.OrderMapper;
import com.fpt.metroll.order.repository.OrderRepository;
import com.fpt.metroll.order.repository.OrderDetailRepository;
import com.fpt.metroll.order.service.OrderService;
import com.fpt.metroll.order.service.PayOSService;
import com.fpt.metroll.shared.domain.client.AccountDiscountPackageClient;
import com.fpt.metroll.shared.domain.client.TicketClient;
import com.fpt.metroll.shared.domain.client.VoucherClient;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.discount.AccountDiscountPackageDto;
import com.fpt.metroll.shared.domain.dto.order.CheckoutItemRequest;
import com.fpt.metroll.shared.domain.dto.order.CheckoutRequest;
import com.fpt.metroll.shared.domain.dto.order.OrderDto;
import com.fpt.metroll.shared.domain.dto.order.OrderDetailDto;
import com.fpt.metroll.shared.domain.dto.ticket.P2PJourneyDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketUpsertRequest;
import com.fpt.metroll.shared.domain.dto.ticket.TimedTicketPlanDto;
import com.fpt.metroll.shared.domain.dto.voucher.VoucherDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.enums.OrderStatus;
import com.fpt.metroll.shared.domain.enums.TicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.exception.NoPermissionException;
import com.fpt.metroll.shared.util.SecurityUtil;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderMapper orderMapper;
    private final TicketClient ticketClient;
    private final VoucherClient voucherClient;
    private final AccountDiscountPackageClient accountDiscountPackageClient;
    private final PayOSService payOSService;

    public OrderServiceImpl(OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            OrderMapper orderMapper,
            TicketClient ticketClient,
            VoucherClient voucherClient,
            AccountDiscountPackageClient accountDiscountPackageClient,
            PayOSService payOSService) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.orderMapper = orderMapper;
        this.ticketClient = ticketClient;
        this.voucherClient = voucherClient;
        this.accountDiscountPackageClient = accountDiscountPackageClient;
        this.payOSService = payOSService;
    }

    @Override
    @Transactional
    public OrderDto checkout(CheckoutRequest checkoutRequest) {
        String currentUserId = SecurityUtil.requireUserId();

        Preconditions.checkNotNull(checkoutRequest, "Checkout request cannot be null");
        Preconditions.checkArgument(checkoutRequest.getItems() != null && !checkoutRequest.getItems().isEmpty(),
                "Checkout items cannot be empty");
        Preconditions.checkArgument(
                checkoutRequest.getPaymentMethod() != null && !checkoutRequest.getPaymentMethod().isBlank(),
                "Payment method cannot be null or blank");

        // Expand checkout items
        checkoutRequest = expandCheckoutItemsByQuantity(checkoutRequest);

        // Determine if this is a staff purchase
        boolean isStaffPurchase = SecurityUtil.hasRole(AccountRole.STAFF, AccountRole.ADMIN) &&
                checkoutRequest.getCustomerId() != null;

        String customerId = isStaffPurchase ? checkoutRequest.getCustomerId() : currentUserId;
        String staffId = isStaffPurchase ? currentUserId : null;

        // Create order details
        List<OrderDetail> orderDetails = new ArrayList<>();
        BigDecimal baseTotal = BigDecimal.ZERO;

        for (CheckoutItemRequest item : checkoutRequest.getItems()) {
            Preconditions.checkNotNull(item.getTicketType(), "Ticket type cannot be null");
            Preconditions.checkArgument(item.getQuantity() > 0, "Quantity must be positive");

            BigDecimal unitPrice;
            String p2pJourneyId = null;
            String timedTicketPlanId = null;

            if (item.getTicketType() == TicketType.P2P) {
                Preconditions.checkArgument(item.getP2pJourneyId() != null && !item.getP2pJourneyId().isBlank(),
                        "P2P Journey ID cannot be null for P2P tickets");
                P2PJourneyDto journey = ticketClient.getP2PJourneyById(item.getP2pJourneyId());
                unitPrice = BigDecimal.valueOf(journey.getBasePrice());
                p2pJourneyId = item.getP2pJourneyId();
            } else if (item.getTicketType() == TicketType.TIMED) {
                Preconditions.checkArgument(
                        item.getTimedTicketPlanId() != null && !item.getTimedTicketPlanId().isBlank(),
                        "Timed Ticket Plan ID cannot be null for TIMED tickets");
                TimedTicketPlanDto plan = ticketClient.getTimedTicketPlanById(item.getTimedTicketPlanId());
                unitPrice = BigDecimal.valueOf(plan.getBasePrice());
                timedTicketPlanId = item.getTimedTicketPlanId();
            } else {
                throw new IllegalArgumentException("Invalid ticket type: " + item.getTicketType());
            }

            BigDecimal itemBaseTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            baseTotal = baseTotal.add(itemBaseTotal);

            OrderDetail orderDetail = OrderDetail.builder()
                    .ticketType(item.getTicketType())
                    .p2pJourney(p2pJourneyId)
                    .timedTicketPlan(timedTicketPlanId)
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .baseTotal(itemBaseTotal)
                    .discountTotal(BigDecimal.ZERO) // Will be calculated later
                    .finalTotal(itemBaseTotal) // Will be updated after discount calculation
                    .build();

            orderDetails.add(orderDetail);
        }

        // Calculate discounts
        BigDecimal totalDiscountAmount = calculateDiscounts(checkoutRequest, baseTotal);

        // Distribute discount proportionally across order details
        distributeDiscount(orderDetails, totalDiscountAmount, baseTotal);

        BigDecimal finalTotal = baseTotal.subtract(totalDiscountAmount);

        // Create order
        Order order = Order.builder()
                .staffId(staffId)
                .customerId(customerId)
                .discountPackage(checkoutRequest.getDiscountPackageId())
                .voucher(checkoutRequest.getVoucherId())
                .baseTotal(baseTotal)
                .discountTotal(totalDiscountAmount)
                .finalTotal(finalTotal)
                .paymentMethod(checkoutRequest.getPaymentMethod())
                .status(OrderStatus.PENDING)
                .transactionReference(generateTransactionReference())
                .orderDetails(orderDetails)
                .build();

        // Set the order reference for each order detail
        for (OrderDetail detail : orderDetails) {
            detail.setOrder(order);
        }

        order = orderRepository.save(order);

        // Create PayOS payment link only for PAYOS payment method
        if ("PAYOS".equals(checkoutRequest.getPaymentMethod())) {
            createPayOSPaymentLink(order);
        } else {
            // For CASH and VNPAY payments, complete the order immediately
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            createTicketsForOrder(order);
        }

        log.info("Created order {} for customer {} with staff {} and final total {}",
                order.getId(), customerId, staffId, finalTotal);
        return convertToDto(order);
    }

    @Override
    public PageDto<OrderDto> getMyOrders(String search, PageableDto pageable) {
        String userId = SecurityUtil.requireUserId();

        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            // Show orders where the user is either the customer or the staff
            var userPredicate = criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("customerId"), userId),
                    criteriaBuilder.equal(root.get("staffId"), userId));

            if (search != null && !search.isBlank()) {
                var searchLower = search.toLowerCase();
                var searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("id")), "%" + searchLower + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("status").as(String.class)),
                                "%" + searchLower + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("paymentMethod")), "%" + searchLower + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("transactionReference")),
                                "%" + searchLower + "%"));
                return criteriaBuilder.and(userPredicate, searchPredicate);
            }

            return userPredicate;
        };

        PageRequest pageRequest = PageRequest.of(
                pageable.getPage(),
                pageable.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> res = orderRepository.findAll(spec, pageRequest);
        Page<OrderDto> dtoPage = res.map(this::convertToDto);

        return PageMapper.INSTANCE.toPageDTO(dtoPage);
    }

    @Override
    public PageDto<OrderDto> getAllOrders(String search, PageableDto pageable) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            if (search != null && !search.isBlank()) {
                var searchLower = search.toLowerCase();
                return criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("id")), "%" + searchLower + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("customerId")), "%" + searchLower + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("staffId")), "%" + searchLower + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("status").as(String.class)),
                                "%" + searchLower + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("paymentMethod")), "%" + searchLower + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("transactionReference")),
                                "%" + searchLower + "%"));
            }
            return criteriaBuilder.conjunction();
        };

        PageRequest pageRequest = PageRequest.of(
                pageable.getPage(),
                pageable.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> res = orderRepository.findAll(spec, pageRequest);
        Page<OrderDto> dtoPage = res.map(this::convertToDto);

        return PageMapper.INSTANCE.toPageDTO(dtoPage);
    }

    @Override
    public Optional<OrderDto> getOrderById(String orderId) {
        Preconditions.checkNotNull(orderId, "Order ID cannot be null");

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();

        // Check permissions
        String currentUserId = SecurityUtil.getUserId();
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF)
                && !Objects.equals(currentUserId, order.getCustomerId())
                && !Objects.equals(currentUserId, order.getStaffId())) {
            throw new NoPermissionException();
        }

        return Optional.of(convertToDto(order));
    }

    @Override
    public OrderDto requireOrderById(String orderId) {
        return getOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Override
    public Optional<OrderDetailDto> getOrderDetailById(String orderDetailId) {
        Preconditions.checkNotNull(orderDetailId, "Order detail ID cannot be null");

        Optional<OrderDetail> orderDetailOpt = orderDetailRepository.findById(orderDetailId);
        if (orderDetailOpt.isEmpty()) {
            return Optional.empty();
        }

        OrderDetail orderDetail = orderDetailOpt.get();

        // Check permissions - user must be able to access the parent order
        String currentUserId = SecurityUtil.getUserId();
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF)) {
            Order order = orderDetail.getOrder();
            if (!Objects.equals(currentUserId, order.getCustomerId())
                    && !Objects.equals(currentUserId, order.getStaffId())) {
                throw new NoPermissionException();
            }
        }

        OrderDetailDto dto = orderMapper.toDetailDto(orderDetail);
        dto.setOrderId(orderDetail.getOrder().getId());
        return Optional.of(dto);
    }

    @Override
    public OrderDetailDto requireOrderDetailById(String orderDetailId) {
        return getOrderDetailById(orderDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Order detail not found"));
    }

    private void createPayOSPaymentLink(Order order) {
        try {
            // Create PayOS payment link
            payOSService.createPaymentLink(order);
            log.info("PayOS payment link created for order {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to create PayOS payment link for order {}: {}", order.getId(), e.getMessage(), e);
            // Set order to FAILED if PayOS payment link creation fails
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);

            // Re-throw the exception to propagate to the API response
            throw e;
        }
    }

    @Transactional
    public void processPaymentCompletion(Order order) {
        try {
            // Update order status to COMPLETED
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);

            // Create tickets for the order
            createTicketsForOrder(order);

            log.info("Payment completed and tickets created for order {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to process payment completion for order {}", order.getId(), e);
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
        }
    }

    private void createTicketsForOrder(Order order) {
        List<TicketUpsertRequest> ticketRequests = new ArrayList<>();

        for (OrderDetail detail : order.getOrderDetails()) {
            for (int i = 0; i < detail.getQuantity(); i++) {
                Instant validUntil = calculateValidUntil(detail);

                TicketUpsertRequest ticketRequest = TicketUpsertRequest.builder()
                        .ticketType(detail.getTicketType())
                        .ticketOrderDetailId(detail.getId())
                        .validUntil(validUntil)
                        .status(TicketStatus.VALID)
                        .build();

                ticketRequests.add(ticketRequest);
            }
        }

        if (!ticketRequests.isEmpty()) {
            try {
                List<TicketDto> createdTickets = ticketClient.createTickets(ticketRequests);
                Map<String, String> ticketIdMap = createdTickets.stream()
                        .collect(Collectors.toMap(TicketDto::getTicketOrderDetailId, TicketDto::getId));

                order.getOrderDetails().forEach(orderDetail -> {
                    String ticketId = ticketIdMap.get(orderDetail.getId());
                    if (ticketId != null) {
                        orderDetail.setTicketId(ticketId);
                    }
                });
                orderRepository.save(order);
                log.info("Created {} tickets for order {}", createdTickets.size(), order.getId());
            } catch (Exception e) {
                log.error("Failed to create tickets for order {}", order.getId(), e);
                // In a real system, this might trigger a compensation flow
            }
        }
    }

    private Instant calculateValidUntil(OrderDetail detail) {
        if (detail.getTicketType() == TicketType.P2P) {
            // P2P tickets are valid for 1 day
            return Instant.now().plus(1, ChronoUnit.DAYS);
        } else if (detail.getTicketType() == TicketType.TIMED && detail.getTimedTicketPlan() != null) {
            // Get the plan duration from the ticket service
            try {
                TimedTicketPlanDto plan = ticketClient.getTimedTicketPlanById(detail.getTimedTicketPlan());
                return Instant.now().plus(plan.getValidDuration(), ChronoUnit.DAYS);
            } catch (Exception e) {
                log.warn("Failed to get timed ticket plan duration for {}, defaulting to 30 days",
                        detail.getTimedTicketPlan());
                return Instant.now().plus(30, ChronoUnit.DAYS);
            }
        }
        return Instant.now().plus(1, ChronoUnit.DAYS);
    }

    private BigDecimal calculateDiscounts(CheckoutRequest checkoutRequest, BigDecimal baseTotal) {
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;

        // Apply discount package if provided
        if (checkoutRequest.getDiscountPackageId() != null) {
            try {
                AccountDiscountPackageDto discountPackage = accountDiscountPackageClient
                        .getAccountDiscountPackage(checkoutRequest.getDiscountPackageId());
                BigDecimal packageDiscount = calculateDiscountPackageDiscount(discountPackage, baseTotal);
                totalDiscountAmount = totalDiscountAmount.add(packageDiscount);
            } catch (Exception e) {
                log.warn("Failed to apply discount package {}: {}",
                        checkoutRequest.getDiscountPackageId(), e.getMessage());
            }
        }

        // Apply voucher if provided
        if (checkoutRequest.getVoucherId() != null) {
            try {
                VoucherDto voucher = voucherClient.getVoucher(checkoutRequest.getVoucherId());
                BigDecimal voucherDiscount = calculateVoucherDiscount(voucher, baseTotal);
                totalDiscountAmount = totalDiscountAmount.add(voucherDiscount);
            } catch (Exception e) {
                log.warn("Failed to apply voucher {}: {}", checkoutRequest.getVoucherId(), e.getMessage());
            }
        }

        return totalDiscountAmount;
    }

    private BigDecimal calculateDiscountPackageDiscount(AccountDiscountPackageDto discountPackage,
            BigDecimal baseTotal) {
        // This is a simplified calculation - in real implementation,
        // you would fetch the discount package details and apply the specific rules
        // For now, assume a 10% discount
        return baseTotal.multiply(BigDecimal.valueOf(0.10));
    }

    private BigDecimal calculateVoucherDiscount(VoucherDto voucher, BigDecimal baseTotal) {
        // Check if voucher is valid and meets minimum transaction amount
        if (voucher.getMinTransactionAmount() != null &&
                baseTotal.compareTo(BigDecimal.valueOf(voucher.getMinTransactionAmount())) < 0) {
            throw new IllegalArgumentException("Order amount does not meet voucher minimum transaction requirement");
        }

        return BigDecimal.valueOf(voucher.getDiscountAmount());
    }

    private void distributeDiscount(List<OrderDetail> orderDetails, BigDecimal totalDiscount, BigDecimal baseTotal) {
        if (totalDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        for (OrderDetail detail : orderDetails) {
            // Calculate proportional discount for this item
            BigDecimal proportion = detail.getBaseTotal().divide(baseTotal, 4, BigDecimal.ROUND_HALF_UP);
            BigDecimal itemDiscount = totalDiscount.multiply(proportion).setScale(2, BigDecimal.ROUND_HALF_UP);

            detail.setDiscountTotal(itemDiscount);
            detail.setFinalTotal(detail.getBaseTotal().subtract(itemDiscount));
        }
    }

    private String generateTransactionReference() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = orderMapper.toDto(order);

        // Set ticketOrderId for each order detail
        if (dto.getOrderDetails() != null) {
            dto.getOrderDetails().forEach(detail -> detail.setOrderId(order.getId()));
        }

        return dto;
    }


    private CheckoutRequest expandCheckoutItemsByQuantity(CheckoutRequest originalRequest) {
        if (originalRequest == null || originalRequest.getItems() == null) {
            return originalRequest;
        }

        List<CheckoutItemRequest> expandedItems = new ArrayList<>();

        for (CheckoutItemRequest item : originalRequest.getItems()) {
            int quantity = item.getQuantity();

            if (quantity == 1) {
                // If quantity is 1, just add the original item
                expandedItems.add(item);
            } else {
                // If quantity > 1, create multiple copies with quantity = 1
                for (int i = 0; i < quantity; i++) {
                    CheckoutItemRequest singleItem = CheckoutItemRequest.builder()
                            .ticketType(item.getTicketType())
                            .p2pJourneyId(item.getP2pJourneyId())
                            .timedTicketPlanId(item.getTimedTicketPlanId())
                            .quantity(1)
                            .build();
                    expandedItems.add(singleItem);
                }
            }
        }

        // Create a new CheckoutRequest with expanded items
        return CheckoutRequest.builder()
                .items(expandedItems)
                .paymentMethod(originalRequest.getPaymentMethod())
                .discountPackageId(originalRequest.getDiscountPackageId())
                .voucherId(originalRequest.getVoucherId())
                .customerId(originalRequest.getCustomerId())
                .build();
    }
}