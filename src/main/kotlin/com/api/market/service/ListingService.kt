package com.api.market.service

import com.api.market.controller.dto.request.ListingCreateRequest
import com.api.market.controller.dto.request.ListingUpdateRequest
import com.api.market.controller.dto.response.ListingResponse
import com.api.market.domain.listing.Listing
import com.api.market.domain.listing.ListingRepository
import com.api.market.event.ListingCanceledEvent
import com.api.market.event.ListingUpdatedEvent
import com.api.market.kafka.KafkaProducer
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class ListingService(
    private val walletApiService: WalletApiService,
    private val listingRepository: ListingRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val kafkaProducer: KafkaProducer,
) {

    fun create(request: ListingCreateRequest): Mono<Listing> {
        return walletApiService.getAccountNftByAddress(request.address, request.nftId)
            .flatMap { nftExists ->
                if (nftExists) {
                    saveListing(request)
                } else {
                    Mono.error(IllegalArgumentException("Invalid NFT ID or NFT ID not found"))
                }
            }
    }

    fun createUpdate(listing: Listing): Mono<Listing> {
        return listingRepository.findById(listing.id!!)
            .map { it.update(listing) }
            .flatMap { listingRepository.save(it) }
            .doOnSuccess { eventPublisher.publishEvent(ListingUpdatedEvent(this,it.toResponse())) }
    }

    fun deleteUpdate(listing: Listing): Mono<Listing> {
        return listingRepository.findById(listing.id!!)
            .map { it.update(listing) }
            .flatMap { listingRepository.save(it) }
            .doOnSuccess { eventPublisher.publishEvent(ListingCanceledEvent(this,it.toResponse())) }
    }


    fun cancel(id: Long) : Mono<Void> {
        return listingRepository.findById(id)
            .map { it.cancel() }
            .flatMap { listingRepository.save(it) }
            .doOnSuccess {
                eventPublisher.publishEvent(ListingCanceledEvent(this, it.toResponse()))
            }.then()
    }


    fun saveListing(request: ListingCreateRequest): Mono<Listing> {
        return listingRepository.existsByNftIdAndAddressAndActiveTrue(request.nftId, request.address)
            .flatMap { exists ->
                if (exists) {
                    Mono.empty()
                } else {
                    val newListing = Listing(
                        nftId = request.nftId,
                        address = request.address,
                        createdDate = request.createdDate.toInstant().toEpochMilli(),
                        endDate = request.endDate.toInstant().toEpochMilli(),
                        active = false, // 아직 리스팅 시작전
                        price = request.price,
                        tokenType = request.tokenType
                    )
                    listingRepository.save(newListing)
                        .doOnSuccess { savedListing ->
                            kafkaProducer.sendListing(savedListing)
                        }
                }
            }
    }

    private fun Listing.toResponse() = ListingResponse (
        id = this.id!!,
        nftId = this.nftId,
        address = this.address,
        createdDateTime = this.createdDate,
        endDateTime =  this.endDate,
        active = this.active,
        price = this.price,
        tokenType = this.tokenType
    )




}