package com.api.market.kafka.stream

import com.api.market.domain.listing.Listing
import com.api.market.kafka.stream.processor.ActivationProcessor
import com.api.market.kafka.stream.processor.ExpirationProcessor
import jakarta.annotation.PostConstruct
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.springframework.kafka.support.serializer.JsonSerde
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.TransformerSupplier
import org.apache.kafka.streams.kstream.ValueTransformerWithKeySupplier
import org.apache.kafka.streams.state.StoreBuilder
import org.apache.kafka.streams.state.Stores
import org.apache.kafka.streams.state.TimestampedKeyValueStore
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Component

@Component
class ListingStreamProcessor(private val streamsBuilder: StreamsBuilder) {

    @PostConstruct
    fun buildPipeline() {
        val listingSerde = JsonSerde(Listing::class.java).apply {
            this.configure(
                mapOf(
                    JsonSerializer.ADD_TYPE_INFO_HEADERS to false
                ), false
            )
        }


        streamsBuilder.addStateStore(
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("activation-store"),
                Serdes.String(),
                listingSerde
            )
        )

        streamsBuilder.addStateStore(
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("expiration-store"),
                Serdes.String(),
                listingSerde
            )
        )

        val listingStream: KStream<String, Listing> = streamsBuilder.stream(
            "listing-events",
            Consumed.with(Serdes.String(), listingSerde)
        )

        val activatedStream = listingStream
            .transform(TransformerSupplier { ActivationProcessor() }, "activation-store")

        activatedStream
            .filter { _, listing -> listing.active }
            .to("activated-listing-events", Produced.with(Serdes.String(), listingSerde))

        // 만료 처리
        val expiredStream = activatedStream
            .transform(TransformerSupplier { ExpirationProcessor() }, "expiration-store")

        expiredStream
            .filter { _, listing -> !listing.active }
            .to("processed-listing-events", Produced.with(Serdes.String(), listingSerde))

    }

}
