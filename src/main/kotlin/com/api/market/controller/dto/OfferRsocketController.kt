package com.api.market.controller.dto

import com.api.market.domain.offer.Offer
import com.api.market.service.AuctionService
import com.api.market.service.OfferService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap

@Controller
class OfferRsocketController(
    private val auctionService: AuctionService,
    private val offerService: OfferService,
) {

    private val roomSinks: ConcurrentHashMap<Long, Sinks.Many<Offer>> = ConcurrentHashMap()

    @MessageMapping("auction.connect")
    fun connect(@Payload auctionId: String): Mono<String> {
        val auctionIdLong = auctionId.toLongOrNull() ?: return Mono.error(IllegalArgumentException("Invalid auction ID"))

        return auctionService.findByActivedAuctionId(auctionIdLong)
            .flatMap {
                println("Client connected to active auction: $auctionIdLong")
                roomSinks.computeIfAbsent(auctionIdLong) { Sinks.many().multicast().onBackpressureBuffer() }
                Mono.just("success")
            }
            .onErrorResume { e ->
                println("Failed to connect to auction: $auctionIdLong, reason: ${e.message}")
                Mono.error(e)
            }
    }

    @MessageMapping("auction.subscribe")
    fun streamOffers(@Payload auctionId: String): Flux<Offer> {
        println("Stream offer to auction: $auctionId")
        return roomSinks.computeIfAbsent(auctionId.toLongOrNull() ?: 0L) { Sinks.many().multicast().onBackpressureBuffer() }.asFlux()
    }

    @MessageMapping("auction.disconnect")
    fun disconnect(@Payload auctionId: String): Mono<String> {
        println("Client disconnected from auction: $auctionId")
        roomSinks.remove(auctionId.toLongOrNull())
        return Mono.just("success")
    }

    init {
        offerService.getOfferCreatedPublisher().subscribe { offer ->
            roomSinks[offer.auctionId]?.tryEmitNext(offer)
        }
    }

}