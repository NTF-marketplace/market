package com.api.market.service

import com.api.market.controller.dto.request.AuctionCreateRequest
import com.api.market.controller.dto.response.AuctionResponse
import com.api.market.controller.dto.response.ListingResponse
import com.api.market.domain.auction.AuctionRepository
import com.api.market.domain.auction.Auction
import com.api.market.domain.listing.Listing
import com.api.market.enums.StatusType
import com.api.market.event.AuctionUpdatedEvent
import com.api.market.event.ListingUpdatedEvent
import com.api.market.kafka.KafkaProducer
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AuctionService(
    private val walletApiService: WalletApiService,
    private val auctionRepository: AuctionRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val kafkaProducer: KafkaProducer,
) {

    fun create(address: String,request: AuctionCreateRequest) : Mono<Auction> {
        return walletApiService.getAccountNftByAddress(address, request.nftId)
            .flatMap { nftExists ->
                if(nftExists) {
                    saveAuction(address,request)
                } else {
                    Mono.error(IllegalArgumentException("Invalid NFT ID"))
                }
            }
            .doOnSuccess { eventPublisher.publishEvent(AuctionUpdatedEvent(this,it.toResponse())) }
    }


    fun update(auction: Auction): Mono<Auction> {
        return auctionRepository.findById(auction.id!!)
            .map { it.update(auction) }
            .flatMap { auctionRepository.save(it) }
            .doOnSuccess { eventPublisher.publishEvent(AuctionUpdatedEvent(this,it.toResponse())) }
    }

    fun saveAuction(address: String,request: AuctionCreateRequest): Mono<Auction> {
        return auctionRepository.existsByNftIdAndAddressAndStatusType(request.nftId, address, StatusType.RESERVATION)
            .flatMap { exists ->
                if (exists) {
                    Mono.empty()
                } else {
                    val newAuction = Auction(
                        nftId = request.nftId,
                        address = address,
                        createdDate = request.createdDate.toInstant().toEpochMilli(),
                        endDate = request.endDate.toInstant().toEpochMilli(),
                        statusType = StatusType.RESERVATION,
                        startingPrice = request.startingPrice,
                        tokenType = request.tokenType
                    )
                    auctionRepository.save(newAuction)
                        .flatMap { savedAuction ->
                            kafkaProducer.sendScheduleEntity("auction-events", savedAuction)
                                .thenReturn(savedAuction)
                        }
                }
            }
    }


    private fun Auction.toResponse() = AuctionResponse (
        id = this.id!!,
        nftId = this.nftId,
        address = this.address,
        createdDateTime = this.createdDate,
        endDateTime =  this.endDate,
        statusType = this.statusType,
        startingPrice = this.startingPrice,
        tokenType = this.tokenType
    )
}