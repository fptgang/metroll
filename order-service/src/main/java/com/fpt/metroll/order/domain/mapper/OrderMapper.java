package com.fpt.metroll.order.domain.mapper;

import com.fpt.metroll.order.document.Order;
import com.fpt.metroll.order.document.OrderDetail;
import com.fpt.metroll.shared.domain.dto.order.OrderDto;
import com.fpt.metroll.shared.domain.dto.order.OrderDetailDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    OrderDto toDto(Order order);
    
    @Mapping(target = "orderId", ignore = true)
    OrderDetailDto toDetailDto(OrderDetail orderDetail);
    
    Order toEntity(OrderDto orderDto);
    
    @Mapping(target = "order", ignore = true)
    OrderDetail toDetailEntity(OrderDetailDto orderDetailDto);
} 