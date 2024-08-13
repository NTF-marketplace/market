package com.api.market.domain.orders.repository

import com.api.market.domain.orders.Orders
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface OrdersRepository : ReactiveCrudRepository<Orders,Long> {
}