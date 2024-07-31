package com.api.market.service

import com.api.market.controller.dto.request.OfferCreateRequest
import com.api.market.domain.auction.AuctionRepository
import com.api.market.domain.offer.Offer
import com.api.market.domain.offer.OfferRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class OfferServce(
    private val offerRepository: OfferRepository,
    private val auctionRepository: AuctionRepository
) {

    fun create(address: String, request: OfferCreateRequest): Mono<Void> {
        return saveOffer(address, request).then()
    }

    //
    // 그건 요청할때마다 offer list(가격 정렬)를 보여주기
    // Rsocket
    //하지만 해당 페이지에 접속하면 세션 접속/ 페이지 나가면 세선 나감
    // 해당 세션에 속해있으면 create될때마다  바로 websocket 으로 던져주기?
    fun saveOffer(address: String,request: OfferCreateRequest): Mono<Offer> {
        return auctionRepository.findById(request.auctionId)
            .switchIfEmpty( Mono.error { IllegalArgumentException("") })
            .flatMap {
            val newOffer = Offer(
                address = address,
                price = request.price,
                auctionId = it.id!!,
            )
            offerRepository.save(newOffer)
        }
    }
}