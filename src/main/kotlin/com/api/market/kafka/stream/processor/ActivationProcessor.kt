package com.api.market.kafka.stream.processor

import com.api.market.domain.listing.Listing
import com.api.market.enums.ListingStatusType
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.Cancellable
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
    private var scheduledPunctuator: Cancellable? = null

    private val logger = LoggerFactory.getLogger(ActivationProcessor::class.java)

    data class ScheduledActivation(val key: String, val activationTime: Long)

    override fun init(context: ProcessorContext) {
        this.context = context
        this.stateStore = context.getStateStore("activation-store") as KeyValueStore<String, Listing>
        scheduleNextActivation()
    }

    override fun transform(key: String, listing: Listing): KeyValue<String, Listing>? {
        val now = context.timestamp()
        logger.info("Transform called - key: $key, status: ${listing.statusType}, current time: $now")

        when (listing.statusType) {
            ListingStatusType.RESERVATION -> {
                if (now >= listing.createdDate) {
                    return activateListing(key, listing)
                } else {
                    scheduleActivation(key, listing)
                }
            }
            ListingStatusType.RESERVATION_CANCEL -> {
                return cancelReservation(key, listing)
            }
            else -> {
                logger.info("Passing through listing - key: $key, status: ${listing.statusType}")
                return KeyValue(key, listing)
            }
        }

        return null
    }

    private fun scheduleActivation(key: String, listing: Listing) {
        stateStore.put(key, listing) // 저장소저장
        scheduledActivations.add(ScheduledActivation(key, listing.createdDate)) // 우선순위 큐에 저장
        logger.info("Scheduled activation - key: $key, activation time: ${listing.createdDate}")
        if (listing.createdDate < nextScheduledTime) { // 다음 스케줄 시간과 비교해서 작다면
            scheduleNextActivation() //  다음 스케줄 할당하고 지연스케줄 실행
        }
    }

    private fun cancelReservation(key: String, listing: Listing): KeyValue<String, Listing> {
        logger.info("Cancelling reservation - key: $key")
        val existingListing = stateStore.get(key)
        if (existingListing != null) {
            stateStore.delete(key)
            logger.info("Removed from state store - key: $key")
            val removed = scheduledActivations.removeIf { it.key == key }
            logger.info("Removed from scheduled activations - key: $key, removed: $removed")
            if (scheduledActivations.isEmpty() || scheduledActivations.peek().activationTime > nextScheduledTime) {
                scheduleNextActivation()
            }
        } else {
            logger.warn("Attempt to cancel non-existent reservation - key: $key")
        }
        return KeyValue(key, listing)
    }


    private fun scheduleNextActivation() {
        scheduledPunctuator?.cancel()

        if (scheduledActivations.isEmpty()) {
            logger.info("큐가 비어있음, 스케줄러 중지 - 현재 시간: ${System.currentTimeMillis()}")
            nextScheduledTime = Long.MAX_VALUE
            return
        }
        val nextActivation = scheduledActivations.peek()
        if (nextActivation!=null) {
            nextScheduledTime = nextActivation.activationTime
            val now = context.timestamp()
            val delay = maxOf(nextScheduledTime - now, minProcessInterval)
            context.schedule(Duration.ofMillis(delay), PunctuationType.WALL_CLOCK_TIME, this::processActivations)
        }
    }

    private fun processActivations(timestamp: Long) {
        var activatedCount = 0
        while (scheduledActivations.isNotEmpty() && scheduledActivations.peek().activationTime <= timestamp) {
            val activation = scheduledActivations.poll()
            val listing = stateStore.get(activation.key)
            if (listing != null && listing.statusType == ListingStatusType.RESERVATION) {
                val activatedListing = listing.copy(statusType = ListingStatusType.LISTING)
                stateStore.put(activation.key, activatedListing)
                context.forward(activation.key, activatedListing)
                activatedCount++
            }
        }
        scheduleNextActivation()
    }

    private fun activateListing(key: String, listing: Listing): KeyValue<String, Listing> {
        val activatedListing = listing.copy(statusType = ListingStatusType.LISTING)
        stateStore.put(key, activatedListing)
        return KeyValue(key, activatedListing)
    }


    override fun close() {}
}