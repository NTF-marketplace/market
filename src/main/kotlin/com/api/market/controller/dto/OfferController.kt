package com.api.market.controller.dto

import com.api.market.controller.dto.request.OfferCreateRequest
import com.api.market.service.OfferService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/offer")
class OfferController(
    private val offerService: OfferService,
) {

    @PostMapping
    fun createOffer(
        @RequestParam address: String,
        @RequestBody request: OfferCreateRequest) : Mono<Void> {
        return offerService.create(address,request)
    }
}