package com.api.market.service

import com.api.market.controller.dto.request.ListingCreateRequest
import com.api.market.controller.dto.response.ListingResponse
import com.api.market.domain.listing.Listing
import com.api.market.domain.listing.ListingRepository
import com.api.market.enums.ListingStatusType
import com.api.market.event.ListingUpdatedEvent
import com.api.market.kafka.KafkaProducer
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

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
            .doOnSuccess { eventPublisher.publishEvent(ListingUpdatedEvent(this,it.toResponse())) }
    }

    fun update(listing: Listing): Mono<Listing> {
        return listingRepository.findById(listing.id!!)
            .map { it.update(listing) }
            .flatMap { listingRepository.save(it) }
           .doOnSuccess { eventPublisher.publishEvent(ListingUpdatedEvent(this,it.toResponse())) }
    }

    fun cancel(id: Long): Mono<Void> {
        return listingRepository.findById(id)
            .doOnNext { listing ->
                val cancelledListing = when (listing.statusType) {
                    ListingStatusType.RESERVATION -> listing.copy(statusType = ListingStatusType.RESERVATION_CANCEL)
                    ListingStatusType.LISTING -> listing.copy(statusType = ListingStatusType.CANCEL)
                    else -> listing
                }
                kafkaProducer.sendCancellation(cancelledListing).subscribe()
            }
            .then()
    }

    fun saveListing(request: ListingCreateRequest): Mono<Listing> {
        return listingRepository.existsByNftIdAndAddressAndStatusType(request.nftId, request.address, ListingStatusType.LISTING)
            .flatMap { exists ->
                if (exists) {
                    Mono.empty()
                } else {
                    val newListing = Listing(
                        nftId = request.nftId,
                        address = request.address,
                        createdDate = request.createdDate.toInstant().toEpochMilli(),
                        endDate = request.endDate.toInstant().toEpochMilli(),
                        statusType = ListingStatusType.RESERVATION, // 아직 리스팅 시작전
                        price = request.price,
                        tokenType = request.tokenType
                    )
                    listingRepository.save(newListing)
                        .doOnSuccess { savedListing ->
                            kafkaProducer.sendListing(savedListing).subscribe()
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
        statusType = this.statusType,
        price = this.price,
        tokenType = this.tokenType
    )
}