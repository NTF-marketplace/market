package com.api.market.kafka

import com.api.market.domain.ScheduleEntity
import com.api.market.service.dto.LedgerRequest
import com.api.market.service.dto.SaleResponse
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord

@Service
class KafkaProducer(
    private val kafkaSender: KafkaSender<String,Any>,
) {

    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)


    fun sendScheduleEntity(topic: String, scheduleEntity: ScheduleEntity): Mono<Void> {
        val record = SenderRecord.create(
            topic,
            null,
            null,
            scheduleEntity.id.toString(),
            scheduleEntity as Any,
            null
        )

        return kafkaSender.send(Mono.just(record))
            .next()
            .doOnSuccess {
                logger.info("request successfully: ${it.recordMetadata()}")
            }.doOnError {
                logger.error("Failed to request", it)
            }.then()
    }


    fun sendCancellation(scheduleEntity: ScheduleEntity): Mono<Void> {
        return sendScheduleEntity("listing-events", scheduleEntity)
    }

    fun sendOrderToLedgerService(request: LedgerRequest): Mono<Void> {
        val record = SenderRecord.create(
            "ledger-topic",
            null,
            null,
            request.orderId.toString(),
            request as Any,
            null,
        )
        return kafkaSender.send(Mono.just(record))
            .next()
            .doOnSuccess {
                logger.info("request successfully: ${it.recordMetadata()}")
            }.doOnError {
                logger.error("Failed to request", it)
            }.then()
    }



    fun sendSaleStatusService(request: SaleResponse): Mono<Void> {
        val record = SenderRecord.create(
            "sale-topic",
            null,
            null,
            request.id.toString(),
            request as Any,
            null
        )

        return kafkaSender.send(Mono.just(record))
            .next()
            .doOnSuccess { result ->
                println("sent sale")
                println("type : ${request.statusType}")
                logger.info("Sent sale request successfully: ${result.recordMetadata()}")
            }
            .doOnError { ex ->
                logger.error("Failed to send sale request", ex)
            }
            .then()
    }
}