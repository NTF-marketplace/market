package com.api.market.kafka

import com.api.market.domain.ScheduleEntity
import com.api.market.domain.auction.Auction
import com.api.market.domain.listing.Listing
import com.api.market.service.AuctionService
import com.api.market.service.ListingService
import com.api.market.service.OrderService
import com.api.market.service.dto.LedgerStatusRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.Message
import org.springframework.stereotype.Service

@Service
class KafkaConsumer(
    private val listingService: ListingService,
    private val auctionService: AuctionService,
    private val orderService: OrderService,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(KafkaConsumer::class.java)


    @KafkaListener(topics = ["activated-events"],
        groupId = "market-group-activated",
        containerFactory = "kafkaListenerContainerFactory")
    fun consumeActivatedEvents(message: Message<Any>) {
        val headers = message.headers
        val payload = message.payload

        if (payload is LinkedHashMap<*, *>) {
            val scheduleEntity = objectMapper.convertValue(payload, ScheduleEntity::class.java)
            updateScheduleEntity(scheduleEntity)
        }
    }

    @KafkaListener(topics = ["processed-events"],
        groupId = "market-group-processed",
        containerFactory = "kafkaListenerContainerFactory")
    fun consumeProcessedEvents(message: Message<Any>) {
        val headers = message.headers
        val payload = message.payload

        logger.info("Received processed event with payload: $payload and headers: $headers")

        if (payload is LinkedHashMap<*, *>) {
            val scheduleEntity = objectMapper.convertValue(payload, ScheduleEntity::class.java)
            updateScheduleEntity(scheduleEntity)
        }
    }

    @KafkaListener(topics = ["ledgerStatus-topic"],
        groupId = "market-group-ledger",
        containerFactory = "kafkaListenerContainerFactory")
    fun consumeLedgerStatusEvents(message: Message<Any>) {
        val headers = message.headers
        val payload = message.payload

        logger.info("Received ledger status event with payload: $payload and headers: $headers")

        if (payload is LinkedHashMap<*, *>) {
            val ledgerStatusRequest = objectMapper.convertValue(payload, LedgerStatusRequest::class.java)
            orderService.updateOrderStatus(ledgerStatusRequest).subscribe()
        }
    }

    private fun updateScheduleEntity(scheduleEntity: ScheduleEntity) {
        when (scheduleEntity) {
            is Listing -> listingService.update(scheduleEntity).subscribe()
            is Auction -> auctionService.update(scheduleEntity).subscribe()
        }
    }
}