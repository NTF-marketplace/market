package com.api.market.kafka

import com.api.market.domain.listing.Listing
import com.api.market.domain.listing.ListingRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class KafkaConsumer(
    private val listingRepository: ListingRepository,
) {
    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)
    @KafkaListener(topics = ["processed-listing-events"], groupId = "market-group", containerFactory = "kafkaListenerContainerFactory")
    fun consumeProcessedListing(@Payload(required = false) listing: Listing?) {
        if (listing == null) {
            logger.error("Received null listing")
            return
        }
        logger.info("Received listing: $listing")
        listingRepository.save(listing).subscribe()
    }
}