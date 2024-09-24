package com.api.market.service

import com.api.market.controller.dto.request.ListingCreateRequest
import com.api.market.domain.listing.Listing
import com.api.market.domain.listing.ListingRepository
import com.api.market.enums.StatusType
import com.api.market.kafka.KafkaProducer
import com.api.market.service.dto.SaleResponse.Companion.toResponse
import com.api.market.service.external.RedisService
import com.api.market.service.external.WalletApiService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ListingService(
    private val walletApiService: WalletApiService,
    private val listingRepository: ListingRepository,
    private val kafkaProducer: KafkaProducer,
    private val redisService: RedisService,
) {

    fun listingHistory(nftId: Long) : Flux<Listing> {
        return listingRepository.findAllByNftIdAndStatusTypeIn(nftId, listOf(StatusType.EXPIRED,StatusType.LEDGER))
    }

    fun create1(address: String,request: ListingCreateRequest): Mono<Listing> {
        return redisService.getNft(request.nftId)
            .switchIfEmpty(Mono.error(IllegalArgumentException("nft not found")))
            .flatMap {
                walletApiService.validNftByAddress(address, request.nftId)
                    .flatMap { nftExists ->
                        if (nftExists) {
                            saveListing(address,request)
                        } else {
                            Mono.error(IllegalArgumentException("Invalid NFT ID or NFT ID not found"))
                        }
                    }
                    .doOnSuccess { listing ->
                        kafkaProducer.sendSaleStatusService(listing.toResponse())
                            .subscribe()
                    }
            }
    }


    fun update(listing: Listing): Mono<Listing> {
        return listingRepository.findById(listing.id!!)
            .map { it.update(listing) }
            .flatMap { listingRepository.save(it) }
            .doOnSuccess {
                kafkaProducer.sendSaleStatusService(listing.toResponse()).subscribe()
            }
    }

    fun cancel(id: Long): Mono<Void> {
        return listingRepository.findById(id)
            .flatMap { listing ->
                val cancelledListing = when (listing.statusType) {
                    StatusType.RESERVATION -> listing.copy(statusType = StatusType.RESERVATION_CANCEL)
                    StatusType.ACTIVED -> listing.copy(statusType = StatusType.CANCEL)
                    else -> listing
                }
                kafkaProducer.sendCancellation(cancelledListing)
                    .then()
            }
            .then()
    }


    fun saveListing(address: String,request: ListingCreateRequest): Mono<Listing> {
        return listingRepository.existsByNftIdAndAddressAndStatusType(request.nftId, address, StatusType.RESERVATION)
            .flatMap { exists ->
                if (exists) {
                    Mono.empty()
                } else {
                    val newListing = Listing(
                        nftId = request.nftId,
                        address = address,
                        createdDate = request.createdDate.toInstant().toEpochMilli(),
                        endDate = request.endDate.toInstant().toEpochMilli(),
                        statusType = StatusType.RESERVATION,
                        price = request.price,
                        chainType = request.chainType
                    )
                    listingRepository.save(newListing)
                        .flatMap { savedListing ->
                            kafkaProducer.sendScheduleEntity("listing-events", savedListing)
                                .thenReturn(savedListing)
                        }
                }
            }
    }

    fun updateStatusLeger(orderId: Long): Mono<Void> {
        return listingRepository.findById(orderId)
            .flatMap { listing ->
                val updatedEntity = listing.updateStatus(StatusType.LEDGER)
                if (updatedEntity is Listing) {
                    update(updatedEntity).then()
                } else {
                    Mono.error(IllegalStateException("Expected a Listing but got ${updatedEntity::class.simpleName}"))
                }
            }
    }

}