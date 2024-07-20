package com.api.market.rabbitMQ

import com.api.market.controller.dto.response.ListingResponse
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class RabbitMQSender(
    private val rabbitTemplate: RabbitTemplate,
) {

    fun listingSend(listing: ListingResponse) {
        println("몇번 보내지는가?")
        rabbitTemplate.convertAndSend("listingExchange", "listingRoutingKey", listing)
    }

    fun listingCancelSend(listing: ListingResponse) {
        rabbitTemplate.convertAndSend("listingCancelExchange", "listingCancelRoutingKey", listing)
    }
}