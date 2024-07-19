package com.api.market.kafka.stream.processor

import com.api.market.domain.listing.Listing
import com.api.market.kafka.KafkaProducer
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

class ActivationProcessor : Transformer<String, Listing, KeyValue<String, Listing>> {
    private lateinit var context: ProcessorContext
    private lateinit var stateStore: KeyValueStore<String, Listing>
    private val scheduledActivations = PriorityQueue<ScheduledActivation>(compareBy { it.activationTime })
    private var nextScheduledTime: Long = Long.MAX_VALUE
    private val minProcessInterval = 1000L

    private val logger = LoggerFactory.getLogger(ActivationProcessor::class.java)

    data class ScheduledActivation(val key: String, val activationTime: Long)

    override fun init(context: ProcessorContext) {
        this.context = context
        this.stateStore = context.getStateStore("activation-store") as KeyValueStore<String, Listing>
        scheduleNextActivation()
    }

    override fun transform(key: String, listing: Listing): KeyValue<String, Listing>? {
        val now = context.timestamp()
        if (now >= listing.createdDate && !listing.active) {
            logger.info("Immediate activation for listing: $key")
            return activateListing(key, listing)
        }

        stateStore.put(key, listing)
        scheduledActivations.add(ScheduledActivation(key, listing.createdDate))
        logger.info("Scheduled activation for listing: $key at ${listing.createdDate}")


        if (listing.createdDate < nextScheduledTime) {
            scheduleNextActivation()
        }

        return null
    }

    private fun scheduleNextActivation() {
        val nextActivation = scheduledActivations.peek()
        if (nextActivation!=null) {
            nextScheduledTime = nextActivation.activationTime
            val now = context.timestamp()
            val delay = maxOf(nextScheduledTime - now, minProcessInterval)
            logger.info("Scheduling next batch activation at: ${now + delay}")
            context.schedule(Duration.ofMillis(delay), PunctuationType.WALL_CLOCK_TIME, this::processActivations)
        }
    }

    private fun processActivations(timestamp: Long) {
        logger.info("Starting batch activation process at: $timestamp")
        var activatedCount = 0
        while (scheduledActivations.isNotEmpty() && scheduledActivations.peek().activationTime <= timestamp) {
            val activation = scheduledActivations.poll()
            val listing = stateStore.get(activation.key)
            if (listing != null && !listing.active) {
                val activatedListing = listing.copy(active = true)
                stateStore.put(activation.key, activatedListing)
                context.forward(activation.key, activatedListing)
                activatedCount++
                logger.info("Batch activated listing: ${activation.key}")
            }
        }
        logger.info("Batch activation completed. Total activated: $activatedCount")
        scheduleNextActivation()
    }

    private fun activateListing(key: String, listing: Listing): KeyValue<String, Listing> {
        val activatedListing = listing.copy(active = true)
        stateStore.put(key, activatedListing)
        return KeyValue(key, activatedListing)
    }

    override fun close() {}
}