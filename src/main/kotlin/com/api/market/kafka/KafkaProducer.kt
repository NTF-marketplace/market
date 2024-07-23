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
        return Mono.create{ sink ->
            val future = kafkaTemplate.send("listing-events", listing.id.toString(), listing)
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

    fun sendCancellation(listing: Listing): Mono<Void> {
        return Mono.fromRunnable {
            val future = kafkaTemplate.send("listing-events", listing.id.toString(), listing)
            future.whenComplete { result, exception ->
                if (exception == null) {
                    logger.info("Sent cancellation message for listing ID: ${listing.id}, offset: ${result.recordMetadata.offset()}")
                } else {
                    logger.error("Failed to send cancellation message for listing ID: ${listing.id}", exception)
                }
            }
        }
    }
}