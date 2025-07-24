package com.fpt.metroll.order.repository;

import com.fpt.metroll.order.document.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, String> {
    List<OrderDetail> findByOrderId(String orderId);

    @Query("""
    SELECT od FROM OrderDetail od
    JOIN od.order o
    WHERE od.p2pJourney IN :p2pJourneyIds
      AND o.status = 'COMPLETED'
    """)
    List<OrderDetail> findByP2pJourneyInAndOrderStatusCompleted(@Param("p2pJourneyIds") List<String> p2pJourneyIds);
}