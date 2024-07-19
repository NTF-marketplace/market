package com.api.market.kafka

import com.api.market.domain.listing.Listing
import com.api.market.domain.listing.ListingRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class KafkaConsumer(
    private val listingRepository: ListingRepository,
) {
    private val logger = LoggerFactory.getLogger(KafkaConsumer::class.java)


    @KafkaListener(topics = ["processed-listing-events"], groupId = "market-group", containerFactory = "kafkaListenerContainerFactory")
    fun consumeProcessedListing(@Payload(required = false) listing: Listing?,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String?,
                                @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int?,
                                @Header(KafkaHeaders.OFFSET) offset: Long?,
                                @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long?) {
        logger.info("Received message - Topic: $topic, Partition: $partition, Offset: $offset, Timestamp: $timestamp")
        if (listing == null) {
            logger.error("Received null listing")
            return
        }
        logger.info("Received listing: $listing")
        listingRepository.save(listing).subscribe()
    }
}