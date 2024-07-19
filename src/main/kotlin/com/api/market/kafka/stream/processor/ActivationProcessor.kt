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

class ActivationProcessor: Transformer<String, Listing, KeyValue<String, Listing>> {
    private lateinit var context: ProcessorContext
    private lateinit var stateStore: KeyValueStore<String, Listing>

    override fun init(context: ProcessorContext) {
        this.context = context
        this.stateStore = context.getStateStore("activation-store") as KeyValueStore<String, Listing>
    }

    override fun transform(key: String, listing: Listing): KeyValue<String, Listing>? {
        val now = context.timestamp()
        if (now >= listing.createdDate && !listing.active) {
            val activatedListing = listing.copy(active = true)
            stateStore.put(key, activatedListing)
            return KeyValue(key, activatedListing)
        }
        stateStore.put(key, listing)
        context.schedule(Duration.ofMillis(listing.createdDate - now), PunctuationType.WALL_CLOCK_TIME) { _ ->
            val storedListing = stateStore.get(key)
            if (storedListing != null && !storedListing.active) {
                val activatedListing = storedListing.copy(active = true)
                stateStore.put(key, activatedListing)
                context.forward(key, activatedListing)
            }
        }
        return null
    }

    override fun close() {}
}