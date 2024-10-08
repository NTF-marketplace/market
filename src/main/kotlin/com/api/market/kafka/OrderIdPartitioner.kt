package com.api.market.kafka

import com.api.market.domain.ScheduleEntity
import com.api.market.service.dto.LedgerRequest
import com.api.market.service.dto.SaleResponse
import org.apache.kafka.clients.producer.Partitioner
import org.apache.kafka.common.Cluster

class OrderIdPartitioner : Partitioner {
    override fun configure(p0: MutableMap<String, *>?) {

    }

    override fun close() {

    }

    override fun partition(
        topic: String,
        key: Any?,
        keyBytes: ByteArray?,
        value: Any?,
        valueBytes: ByteArray?,
        cluster: Cluster
    ): Int {
        val partitions = cluster.partitionsForTopic(topic)
        val numPartitions = partitions.size

        val orderId = when (value) {
            is LedgerRequest -> value.orderId
            is ScheduleEntity -> value.id ?: throw IllegalArgumentException("ScheduleEntity ID is null")
            is SaleResponse -> value.id
            else -> throw IllegalArgumentException("Unexpected value type: ${value?.javaClass}")
        }

        return (orderId % numPartitions).toInt()
    }
}