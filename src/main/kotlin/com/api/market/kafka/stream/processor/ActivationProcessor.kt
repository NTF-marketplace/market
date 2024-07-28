package com.api.market.kafka.stream.processor

import com.api.market.domain.ScheduleEntity
import com.api.market.enums.StatusType
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.Cancellable
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

class ActivationProcessor : Transformer<String, ScheduleEntity, KeyValue<String, ScheduleEntity>> {
    private lateinit var context: ProcessorContext
    private lateinit var stateStore: KeyValueStore<String, ScheduleEntity>
    private val scheduledActivations = PriorityQueue<ScheduledActivation>(compareBy { it.activationTime })
    private var nextScheduledTime: Long = Long.MAX_VALUE
    private val minProcessInterval = 1000L
    private var scheduledPunctuator: Cancellable? = null

    private val logger = LoggerFactory.getLogger(ActivationProcessor::class.java)

    data class ScheduledActivation(val key: String, val activationTime: Long)

    override fun init(context: ProcessorContext) {
        this.context = context
        this.stateStore = context.getStateStore("activation-store") as KeyValueStore<String, ScheduleEntity>
        scheduleNextActivation()
    }

    override fun transform(key: String, scheduleEntity: ScheduleEntity): KeyValue<String, ScheduleEntity>? {
        val now = context.timestamp()
        logger.info("Transform called - key: $key, status: ${scheduleEntity.statusType}, current time: $now")

        when (scheduleEntity.statusType) {
            StatusType.RESERVATION -> {
                if (now >= scheduleEntity.createdDate) {
                    return activateScheduleEntity(key, scheduleEntity)
                } else {
                    scheduleActivation(key, scheduleEntity)
                }
            }
            StatusType.RESERVATION_CANCEL -> {
                return cancelReservation(key, scheduleEntity)
            }
            else -> {
                logger.info("Passing through - key: $key, status: ${scheduleEntity.statusType}")
                return KeyValue(key, scheduleEntity)
            }
        }

        return null
    }

    private fun scheduleActivation(key: String, scheduleEntity: ScheduleEntity) {
        stateStore.put(key, scheduleEntity) // 저장소 저장
        scheduledActivations.add(ScheduledActivation(key, scheduleEntity.createdDate)) // 우선순위 큐에 저장
        logger.info("Scheduled activation - key: $key, activation time: ${scheduleEntity.createdDate}")
        if (scheduleEntity.createdDate < nextScheduledTime) { // 다음 스케줄 시간과 비교해서 작다면
            scheduleNextActivation() //  다음 스케줄 할당하고 지연 스케줄 실행
        }
    }


    private fun cancelReservation(key: String,  scheduleEntity: ScheduleEntity): KeyValue<String, ScheduleEntity> {
        logger.info("Cancelling reservation - key: $key")
        val existingScheduleEntity = stateStore.get(key)
        if (existingScheduleEntity != null) {
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
        return KeyValue(key, scheduleEntity)
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
            val scheduleEntity = stateStore.get(activation.key)
            if (scheduleEntity != null && scheduleEntity.statusType == StatusType.RESERVATION) {
                val activationScheduleEntity = scheduleEntity.updateStatus(StatusType.ACTIVED)
                stateStore.put(activation.key, activationScheduleEntity)
                context.forward(activation.key, activationScheduleEntity)
                activatedCount++
            }
        }
        scheduleNextActivation()
    }

    private fun activateScheduleEntity(key: String, scheduleEntity: ScheduleEntity): KeyValue<String, ScheduleEntity> {
        val activatedSchedule = scheduleEntity.updateStatus(StatusType.ACTIVED)
        stateStore.put(key, activatedSchedule)
        return KeyValue(key, activatedSchedule)
    }

    override fun close() {}
}