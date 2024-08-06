package com.api.market.kafka.stream

import com.api.market.domain.ScheduleEntity
import com.api.market.enums.StatusType
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
import org.apache.kafka.streams.state.Stores
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Component

@Component
class StreamProcessor(private val streamsBuilder: StreamsBuilder) {

    @PostConstruct
    fun buildPipeline() {
        val scheduleEntitySerde = JsonSerde(ScheduleEntity::class.java).apply {
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
                scheduleEntitySerde
            )
        )

        streamsBuilder.addStateStore(
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("expiration-store"),
                Serdes.String(),
                scheduleEntitySerde
            )
        )

        val listingStream: KStream<String, ScheduleEntity> = streamsBuilder.stream(
            "listing-events",
            Consumed.with(Serdes.String(), scheduleEntitySerde)
        )

        val auctionStream: KStream<String,ScheduleEntity > = streamsBuilder.stream(
            "auction-events",
            Consumed.with(Serdes.String(), scheduleEntitySerde)
        )


        val allStream = listingStream.merge(auctionStream)

        val activatedStream = allStream
            .transform(TransformerSupplier { ActivationProcessor() }, "activation-store")

        activatedStream
            .filter { _, scheduleEntity ->
                scheduleEntity.statusType == StatusType.RESERVATION ||
                        scheduleEntity.statusType == StatusType.RESERVATION_CANCEL
            }
            .to("activated-events", Produced.with(Serdes.String(), scheduleEntitySerde))

        // 만료 처리
        val expiredStream = activatedStream
            .transform(TransformerSupplier { ExpirationProcessor() }, "expiration-store")

        expiredStream
            .filter { _, listing ->
                listing.statusType == StatusType.ACTIVED ||
                        listing.statusType == StatusType.CANCEL || listing.statusType == StatusType.EXPIRED
            }
            .to("processed-events", Produced.with(Serdes.String(), scheduleEntitySerde))
    }

}


