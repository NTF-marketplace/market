package com.api.market.config

import com.api.market.domain.listing.Listing
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.processor.WallclockTimestampExtractor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerde
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafkaStreams
class KafkaConfig {

    private val logger = LoggerFactory.getLogger(KafkaConfig::class.java)

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean
    fun listingEventsTopic(): NewTopic = TopicBuilder.name("listing-events")
        .partitions(4)
        .replicas(2)
        .build()

    @Bean
    fun processedListingEventsTopic(): NewTopic = TopicBuilder.name("processed-listing-events")
        .partitions(4)
        .replicas(2)
        .build()

    @Bean
    fun activatedListingEventsTopic(): NewTopic = TopicBuilder.name("activated-listing-events")
        .partitions(4)
        .replicas(2)
        .build()

    @Bean
    fun producerFactory(): ProducerFactory<String, Listing> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Listing> {
        return KafkaTemplate(producerFactory())
    }

    // consumer-------------------------
    @Bean
    fun consumerFactory(): ConsumerFactory<String, Listing> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "market-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java.name,
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            JsonDeserializer.VALUE_DEFAULT_TYPE to Listing::class.java.name
        )
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), JsonDeserializer(Listing::class.java, false))
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Listing> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Listing>()
        factory.consumerFactory = consumerFactory()
        factory.setConcurrency(4)
        factory.setCommonErrorHandler(object : CommonErrorHandler {
            override fun handleRemaining(thrownException: Exception, records: List<org.apache.kafka.clients.consumer.ConsumerRecord<*, *>>, consumer: org.apache.kafka.clients.consumer.Consumer<*, *>, container: MessageListenerContainer) {
                logger.error("Error in consumer: ${thrownException.message}", thrownException)
                logger.error("Problematic records: $records")
            }
        })
        return factory
    }
    // -------------------------------------
    @Bean(name = ["defaultKafkaStreamsConfig"])
    fun kStreamsConfig(): KafkaStreamsConfiguration {
        val props = mapOf(
            StreamsConfig.APPLICATION_ID_CONFIG to "market-streams",
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String()::class.java,
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to JsonSerde::class.java,
            StreamsConfig.PROCESSING_GUARANTEE_CONFIG to StreamsConfig.EXACTLY_ONCE_V2,
            StreamsConfig.COMMIT_INTERVAL_MS_CONFIG to "100",
            StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG to WallclockTimestampExtractor::class.java,
            StreamsConfig.NUM_STREAM_THREADS_CONFIG to 4

        )
        return KafkaStreamsConfiguration(props)
    }

}

