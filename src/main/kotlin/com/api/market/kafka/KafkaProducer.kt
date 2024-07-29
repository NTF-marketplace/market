package com.api.market.kafka

import com.api.market.domain.ScheduleEntity
import com.api.market.service.dto.LedgerRequest
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)

    fun sendScheduleEntity(topic: String, scheduleEntity: ScheduleEntity): Mono<Void> {
        return Mono.create { sink ->
            val future = kafkaTemplate.send(topic, scheduleEntity.id.toString(), scheduleEntity)
            future.whenComplete { result, ex ->
                if (ex == null) {
                    logger.info("Sent successfully: ${result?.recordMetadata}")
                    sink.success()
                } else {
                    logger.error("Failed to send", ex)
                    sink.error(ex)
                }
            }
        }
    }

    fun sendCancellation(scheduleEntity: ScheduleEntity): Mono<Void> {
        return sendScheduleEntity("listing-events", scheduleEntity)
    }


    fun sendOrderToLedgerService(request: LedgerRequest): Mono<Void> {
        return Mono.create { sink ->
            val future = kafkaTemplate.send("ledger-topic", request)
            future.whenComplete { result, ex ->
                if (ex == null) {
                    logger.info("Sent ledger request successfully: ${result?.recordMetadata}")
                    sink.success()
                } else {
                    logger.error("Failed to send ledger request", ex)
                    sink.error(ex)
                }
            }
        }
    }
}