package com.api.market.domain.orders.repository

import com.api.market.domain.orders.Orders
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface OrdersRepository : R2dbcRepository<Orders,Long> {
}