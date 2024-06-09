package com.api.market.rabbitMQ

import com.api.market.service.NftService
import com.api.market.service.dto.NftResponse
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class RabbitMQReceiver(
    private val nftService: NftService,
) {
    @RabbitListener(queues = ["nftQueue"])
    fun nftMessage(nft: NftResponse) {
        nftService.save(nft)
            .subscribe()
    }
}