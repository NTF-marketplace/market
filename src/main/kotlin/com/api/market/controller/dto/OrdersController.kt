package com.api.market.controller.dto

import com.api.market.controller.dto.response.OrdersResponse
import com.api.market.domain.offer.Offer
import com.api.market.domain.orders.Orders
import com.api.market.service.OrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/v1/orders")
class OrdersController(
    private val orderService: OrderService
) {

    @GetMapping("/history")
    fun orderHistory(@RequestParam nftId: Long): Flux<OrdersResponse> {
        return orderService.orderHistoryByNftId(nftId)
    }
}