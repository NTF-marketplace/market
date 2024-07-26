package com.api.market.kafka

import com.api.market.domain.ScheduleEntity
import com.api.market.domain.auction.Auction
import com.api.market.domain.listing.Listing
import com.api.market.service.AuctionService
import com.api.market.service.ListingService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class KafkaConsumer(
    private val listingService: ListingService,
    private val auctionService: AuctionService,
) {
    private val logger = LoggerFactory.getLogger(KafkaConsumer::class.java)


    @KafkaListener(topics = ["activated-events"],
        groupId = "market-group-activated",
        containerFactory = "kafkaListenerContainerFactory")
    fun consumeActivatedEvents(@Payload(required = false) scheduleEntity: ScheduleEntity?,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String?,
                               @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int?,
                               @Header(KafkaHeaders.OFFSET) offset: Long?,
                               @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long?) {
        if (scheduleEntity == null) {
            logger.error("Received null activated event")
            return
        }
        println("consumer : " + scheduleEntity.statusType)
        updateScheduleEntity(scheduleEntity)
    }
    @KafkaListener(topics = ["processed-events"],
        groupId = "market-group-processed",
        containerFactory = "kafkaListenerContainerFactory")
    fun consumeProcessedEvents(@Payload(required = false) scheduleEntity: ScheduleEntity?,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String?,
                               @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int?,
                               @Header(KafkaHeaders.OFFSET) offset: Long?,
                               @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long?) {
        if (scheduleEntity == null) {
            logger.error("Received null processed event")
            return
        }
        updateScheduleEntity(scheduleEntity)
    }


    private fun updateScheduleEntity(scheduleEntity: ScheduleEntity) {
        when (scheduleEntity) {
            is Listing -> listingService.update(scheduleEntity).subscribe()
            //is Auction -> auctionService.update(scheduleEntity).subscribe()
        }
    }
}