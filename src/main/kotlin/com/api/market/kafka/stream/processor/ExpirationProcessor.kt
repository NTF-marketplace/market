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

    override fun init(context: ProcessorContext) {
        this.context = context
        this.stateStore = context.getStateStore("expiration-store") as KeyValueStore<String, Listing>
    }

    override fun transform(key: String, listing: Listing): KeyValue<String, Listing>? {
        println("ExpirationProcessor - inside transform key: $key")
        println("ExpirationProcessor - inside transform listing: $listing")
        val now = context.timestamp()
        if (now >= listing.endDate && listing.active) {
            val expiredListing = listing.copy(active = false)
            stateStore.put(key, expiredListing)
            return KeyValue(key, expiredListing)
        }
        stateStore.put(key, listing)
        context.schedule(Duration.ofMillis(listing.endDate - now), PunctuationType.WALL_CLOCK_TIME) { _ ->
            println("ExpirationProcessor - execute schedule")
            val storedListing = stateStore.get(key)
            println("ExpirationProcessor - storedListing: $storedListing")
            if (storedListing != null && storedListing.active) {
                println("ExpirationProcessor - Expiring listing")
                val expiredListing = storedListing.copy(active = false)
                println("ExpirationProcessor - expiredListing: $expiredListing")
                stateStore.put(key, expiredListing)
                context.forward(key, expiredListing)
            }
        }
        return null
    }

    override fun close() {}
}