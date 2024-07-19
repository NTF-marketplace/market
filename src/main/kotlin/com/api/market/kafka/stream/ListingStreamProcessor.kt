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


        // 상태 저장소 추가
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

        // 활성화 처리
        val activatedStream = listingStream
            .transform(TransformerSupplier { ActivationProcessor() }, "activation-store")

        // 만료 처리
        val expiredStream = listingStream
            .transform(TransformerSupplier { ExpirationProcessor() }, "expiration-store")

        // 두 스트림 병합
        val mergedStream = activatedStream.merge(expiredStream)

        mergedStream.to("processed-listing-events", Produced.with(Serdes.String(), listingSerde))
    }

}
