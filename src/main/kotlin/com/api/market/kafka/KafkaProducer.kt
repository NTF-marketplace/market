package com.api.market.kafka

import com.api.market.domain.listing.Listing
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, Listing>
) {

    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)

    fun sendListing(listing: Listing): Mono<Void> {
        return Mono.create { sink ->
            val future = kafkaTemplate.send("listing-events", listing.nftId.toString(), listing)
            future.whenComplete { result, ex ->
                if (ex == null) {
                    logger.info("Sent listing successfully: ${result?.recordMetadata}")
                    sink.success()
                } else {
                    logger.error("Failed to send listing", ex)
                    sink.error(ex)
                }
            }
        }
    }
}