package com.api.market.kafka

import com.api.market.domain.listing.Listing
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
) {
    private val logger = LoggerFactory.getLogger(KafkaConsumer::class.java)

    @KafkaListener(topics = ["activated-listing-events"],
        groupId = "market-group-activated",
        containerFactory = "kafkaListenerContainerFactory")
    fun consumeActivatedListings(@Payload(required = false) listing: Listing?,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String?,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int?,
                                 @Header(KafkaHeaders.OFFSET) offset: Long?,
                                 @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long?) {
        if (listing == null) {
            logger.error("Received null activated listing")
            return
        }
        updateListing(listing)
    }

    @KafkaListener(topics = ["processed-listing-events"],
        groupId = "market-group-processed",
        containerFactory = "kafkaListenerContainerFactory")
    fun consumeProcessedListings(@Payload(required = false) listing: Listing?,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String?,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int?,
                                 @Header(KafkaHeaders.OFFSET) offset: Long?,
                                 @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long?) {
        if (listing == null) {
            logger.error("Received null processed listing")
            return
        }
        updateListing(listing)
    }

    private fun updateListing(listing: Listing) {
        if(listing.active){
            listingService.createUpdate(listing).subscribe()
        }
        else {
            listingService.deleteUpdate(listing).subscribe()
        }
    }
}