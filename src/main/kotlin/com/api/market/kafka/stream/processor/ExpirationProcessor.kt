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

class ExpirationProcessor : Transformer<String, ScheduleEntity, KeyValue<String, ScheduleEntity>> {
    private lateinit var context: ProcessorContext
    private lateinit var stateStore: KeyValueStore<String, ScheduleEntity>
    private val scheduledExpirations = PriorityQueue<ScheduledExpiration>(compareBy { it.expirationTime })
    private var nextScheduledTime: Long = Long.MAX_VALUE
    private val minProcessInterval = 1000L
    private var scheduledPunctuator: Cancellable? = null

    private val logger = LoggerFactory.getLogger(ExpirationProcessor::class.java)

    data class ScheduledExpiration(val key: String, val expirationTime: Long)

    override fun init(context: ProcessorContext) {
        this.context = context
        this.stateStore = context.getStateStore("expiration-store") as KeyValueStore<String, ScheduleEntity>
        scheduleNextExpiration()
    }

    override fun transform(key: String, scheduleEntity: ScheduleEntity): KeyValue<String, ScheduleEntity> {
        val now = context.timestamp()
        logger.info("Transform called - key: $key, status: ${scheduleEntity.statusType}, current time: $now")

        when (scheduleEntity.statusType) {
            StatusType.ACTIVED -> {
                if (now >= scheduleEntity.endDate) {
                    return expireScheduleEntity(key, scheduleEntity)
                } else {
                    scheduleExpiration(key, scheduleEntity)
                }
            }
            StatusType.CANCEL -> {
                return cancelScheduleEntity(key, scheduleEntity)
            }
            else -> {
                logger.info("Passing through scheduleEntity - key: $key, status: ${scheduleEntity.statusType}")
            }
        }

        return KeyValue(key, scheduleEntity)
    }

    private fun scheduleExpiration(key: String, scheduleEntity: ScheduleEntity) {
        stateStore.put(key, scheduleEntity)
        scheduledExpirations.add(ScheduledExpiration(key, scheduleEntity.endDate))
        logger.info("Scheduled expiration - key: $key, expiration time: ${scheduleEntity.endDate}")
        if (scheduleEntity.endDate < nextScheduledTime) {
            scheduleNextExpiration()
        }
    }
    private fun cancelScheduleEntity(key: String, scheduleEntity: ScheduleEntity): KeyValue<String, ScheduleEntity> {
        logger.info("Cancelling - key: $key")
        val existingScheduleEntity = stateStore.get(key)
        if (existingScheduleEntity != null) {
            stateStore.delete(key)
            logger.info("Removed from state store - key: $key")
            val removed = scheduledExpirations.removeIf { it.key == key }
            logger.info("Removed from scheduled expirations - key: $key, removed: $removed")
            if (scheduledExpirations.isEmpty() || scheduledExpirations.peek().expirationTime > nextScheduledTime) {
                scheduleNextExpiration()
            }
        } else {
            logger.info("No scheduleEntity found in expiration store for cancelled scheduleEntity: $key")
        }
        return KeyValue(key, scheduleEntity)
    }
    private fun scheduleNextExpiration() {
        scheduledPunctuator?.cancel()

        if (scheduledExpirations.isEmpty()) {
            logger.info("큐가 비어있음, 스케줄러 중지 - 현재 시간: ${System.currentTimeMillis()}")
            nextScheduledTime = Long.MAX_VALUE
            return
        }
        val nextExpiration = scheduledExpirations.peek()
        if (nextExpiration != null) {
            nextScheduledTime = nextExpiration.expirationTime
            val now = context.timestamp()
            val delay = maxOf(nextScheduledTime - now, minProcessInterval)

            context.schedule(Duration.ofMillis(delay), PunctuationType.WALL_CLOCK_TIME, this::processExpirations)
        }
    }

    private fun processExpirations(timestamp: Long) {
        var expiredCount = 0
        while (scheduledExpirations.isNotEmpty() && scheduledExpirations.peek().expirationTime <= timestamp) {
            val expiration = scheduledExpirations.poll()
            val scheduleEntity = stateStore.get(expiration.key)
            if (scheduleEntity != null && scheduleEntity.statusType == StatusType.ACTIVED) {
                val expiredScheduleEntity = expireScheduleEntity(expiration.key, scheduleEntity).value
                context.forward(expiration.key, expiredScheduleEntity)
                expiredCount++
            } else {
                stateStore.delete(expiration.key)
            }
        }
        scheduleNextExpiration()
    }

    private fun expireScheduleEntity(key: String, scheduleEntity: ScheduleEntity): KeyValue<String, ScheduleEntity> {
        val expiredSchedule = scheduleEntity.updateStatus(statusType = StatusType.EXPIRED)
        stateStore.put(key, expiredSchedule)
        return KeyValue(key, expiredSchedule)
    }

    override fun close() {}
}