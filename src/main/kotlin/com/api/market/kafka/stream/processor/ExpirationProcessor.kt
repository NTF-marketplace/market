package com.api.market.kafka.stream.processor

import com.api.market.domain.listing.Listing
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.kstream.ValueTransformerWithKey
import org.apache.kafka.streams.processor.Cancellable
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.TimestampedKeyValueStore
import org.apache.kafka.streams.state.ValueAndTimestamp
import java.time.Duration

class ExpirationProcessor : Transformer<String, Listing, KeyValue<String, Listing>> {
    private lateinit var context: ProcessorContext
    private lateinit var stateStore: KeyValueStore<String, Listing>
    private lateinit var scheduler: Cancellable

    override fun init(context: ProcessorContext) {
        this.context = context
        this.stateStore = context.getStateStore("expiration-store") as KeyValueStore<String, Listing>
        this.scheduler = context.schedule(Duration.ofSeconds(10), PunctuationType.WALL_CLOCK_TIME, this::processStoredListings)
    }

    override fun transform(key: String, value: Listing): KeyValue<String, Listing>? {
        stateStore.put(key, value)
        return null // Don't emit anything immediately
    }

    private fun processStoredListings(timestamp: Long) {
        val iter = stateStore.all()
        while (iter.hasNext()) {
            val entry = iter.next()
            val listing = entry.value
            if (timestamp >= listing.endDate && listing.active) {
                val expiredListing = listing.copy(active = false)
                stateStore.put(entry.key, expiredListing)
                context.forward(entry.key, expiredListing)
            }
        }
        iter.close()
    }

    override fun close() {
        scheduler.cancel()
    }
}