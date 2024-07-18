package com.api.market.service

import com.api.market.controller.dto.request.ListingCreateRequest
import com.api.market.controller.dto.request.ListingUpdateRequest
import com.api.market.controller.dto.response.ListingResponse
import com.api.market.domain.listing.Listing
import com.api.market.domain.listing.ListingRepository
import com.api.market.event.ListingCanceledEvent
import com.api.market.event.ListingUpdatedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class ListingService(
    private val walletApiService: WalletApiService,
    private val listingRepository: ListingRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {

    fun getPriceHistory(nftId: Long) : Flux<ListingResponse> {
        return listingRepository.findAllByNftIdOrderByCreatedAt(nftId).map { it.toResponse() }
    }

    fun getListingByNftId(nftId: Long): Mono<ListingResponse> {
        return listingRepository.findByNftIdAndActiveTrue(nftId).map { it.toResponse() }
    }


    fun create(request: ListingCreateRequest): Mono<Listing> {
        return walletApiService.getAccountNftByAddress(request.address, request.nftId)
            .flatMap { nftExists ->
                if (nftExists) {
                    saveListing(request)
                } else {
                    Mono.error(IllegalArgumentException("Invalid NFT ID or NFT ID not found"))
                }
            }
            // .doOnSuccess { listing ->
            //     // 그 시간에 해야되는거 아니야?
            //     eventPublisher.publishEvent(ListingUpdatedEvent(this, listing.toResponse()))
            // }
    }


    fun update(id : Long, request: ListingUpdateRequest): Mono<Listing> {
        return listingRepository.findById(id)
            .map { it.update(request) }
            .flatMap { listingRepository.save(it) }
            .doOnSuccess { eventPublisher.publishEvent(ListingUpdatedEvent(this,it.toResponse())) }
    }

    fun cancel(id: Long) : Mono<Void> {
        return listingRepository.findById(id)
            .map { it.cancel() }
            .flatMap { listingRepository.save(it) }
            .doOnSuccess {
                eventPublisher.publishEvent(ListingCanceledEvent(this, listOf(id)))
            }.then()
    }

    fun batchCancel(time: Long) : Mono<Void> {
        return listingRepository.findAllByEndDateLessThanEqualAndActiveTrueOrderByEndDateAsc(time)
            .map { it.cancel() }
            .flatMap { listingRepository.save(it) }
            .mapNotNull { it.nftId }
            .collectList()
            .doOnSuccess {
                eventPublisher.publishEvent(ListingCanceledEvent(this, it))
            }.then()
    }

    fun saveListing(request: ListingCreateRequest): Mono<Listing> {
        return listingRepository.existsByNftIdAndAddressAndActiveTrue(request.nftId, request.address).flatMap {
            if (it) {
                Mono.empty()
            } else {
                // history 같은것도 저장하게 해야겠다.
                listingRepository.save(
                    Listing(
                        nftId = request.nftId,
                        address = request.address,
                        endDate = request.endDate.toInstant().toEpochMilli(),
                        active = true,
                        price = BigDecimal(request.price),
                        tokenType = request.tokenType
                    )
                )
            }
        }
    }

    private fun Listing.toResponse() = ListingResponse (
        id = this.id!!,
        nftId = this.nftId,
        address = this.address,
        createdDateTime = this.createdAt!!,
        endDateTime =  this.endDate,
        price = this.price,
        tokenType = this.tokenType
    )




}