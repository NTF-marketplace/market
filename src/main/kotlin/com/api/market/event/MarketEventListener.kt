package com.api.market.event


import com.api.market.rabbitMQ.RabbitMQSender
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class MarketEventListener(
    private val provider: RabbitMQSender,
) {
    @EventListener
    fun listingUpdated(event: ListingUpdatedEvent) {
        provider.listingSend(event.listing)
    }
}