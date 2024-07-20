package com.api.market.kafka.stream.processor

import com.api.market.domain.listing.Listing
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

class ExpirationProcessor : Transformer<String, Listing, KeyValue<String, Listing>> {
    private lateinit var context: ProcessorContext
    private lateinit var stateStore: KeyValueStore<String, Listing>
    private val scheduledExpirations = PriorityQueue<ScheduledExpiration>(compareBy { it.expirationTime })
    private var nextScheduledTime: Long = Long.MAX_VALUE
    private val minProcessInterval = 1000L

    private val logger = LoggerFactory.getLogger(ExpirationProcessor::class.java)

    data class ScheduledExpiration(val key: String, val expirationTime: Long)

    override fun init(context: ProcessorContext) {
        this.context = context
        this.stateStore = context.getStateStore("expiration-store") as KeyValueStore<String, Listing>
        scheduleNextExpiration()
    }

    override fun transform(key: String, listing: Listing): KeyValue<String, Listing> {
        val now = context.timestamp()
        logger.info("ExpirationProcessor received listing: $key, endDate: ${listing.endDate}, now: $now, active: ${listing.active}")

        if (now >= listing.endDate && listing.active) {
            logger.info("Immediate expiration for listing: $key")
            return expireListing(key, listing)
        }

        if (listing.active) {
            stateStore.put(key, listing)
            scheduledExpirations.add(ScheduledExpiration(key, listing.endDate))

            if (listing.endDate < nextScheduledTime) {
                scheduleNextExpiration()
            }
        }

        // 변경되지 않은 리스팅도 그대로 전달
        return KeyValue(key, listing)
    }



    private fun scheduleNextExpiration() {
        val nextExpiration = scheduledExpirations.peek()
        if (nextExpiration != null) {
            nextScheduledTime = nextExpiration.expirationTime
            val now = context.timestamp()
            val delay = maxOf(nextScheduledTime - now, minProcessInterval)
            logger.info("Scheduling next batch expiration at: ${now + delay}")
            context.schedule(Duration.ofMillis(delay), PunctuationType.WALL_CLOCK_TIME, this::processExpirations)
        }
    }

    private fun processExpirations(timestamp: Long) {
        logger.info("Starting batch expiration process at: $timestamp")
        var expiredCount = 0
        while (scheduledExpirations.isNotEmpty() && scheduledExpirations.peek().expirationTime <= timestamp) {
            val expiration = scheduledExpirations.poll()
            val listing = stateStore.get(expiration.key)
            if (listing != null && listing.active) {
                val expiredListing = expireListing(expiration.key, listing).value
                context.forward(expiration.key, expiredListing)
                expiredCount++
                logger.info("Batch expired listing: ${expiration.key}")
            } else {
                // 이미 만료되었거나 존재하지 않는 리스팅 제거
                stateStore.delete(expiration.key)
            }
        }
        logger.info("Batch expiration completed. Total expired: $expiredCount")
        scheduleNextExpiration()
    }

    private fun expireListing(key: String, listing: Listing): KeyValue<String, Listing> {
        val expiredListing = listing.copy(active = false)
        stateStore.put(key, expiredListing)
        return KeyValue(key, expiredListing)
    }

    override fun close() {}
}