package com.api.market.domain.orders.repository

import com.api.market.domain.orders.Orders
import com.api.market.enums.OrderStatusType
import com.api.market.enums.OrderType
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface OrdersRepository : ReactiveCrudRepository<Orders,Long> {
    fun findAllByOrderableIdInAndOrderTypeAndOrderStatusType(
        orderableId: Collection<Long>,
        orderType: OrderType,
        status: OrderStatusType
    ): Flux<Orders>
}