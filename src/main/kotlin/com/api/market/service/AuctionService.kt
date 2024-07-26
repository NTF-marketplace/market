package com.api.market.service

import com.api.market.controller.dto.request.AuctionCreateRequest
import com.api.market.domain.auction.AuctionRepository
import com.api.market.domain.auction.Auction
import com.api.market.enums.StatusType
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AuctionService(
    private val walletApiService: WalletApiService,
    private val auctionRepository: AuctionRepository,
) {

    fun create(request: AuctionCreateRequest) : Mono<Auction> {
        return walletApiService.getAccountNftByAddress(request.address, request.nftId)
            .flatMap { nftExists ->
                if(nftExists) {
                    saveAuction(request)
                } else {
                }
                Mono.error(IllegalArgumentException("Invalid NFT ID"))
            }
    }

    fun saveAuction(request: AuctionCreateRequest): Mono<Auction> {
        return auctionRepository.existsByNftIdAndAddressAndStatusType(request.nftId, request.address, statusType = StatusType.RESERVATION)
            .flatMap { exists ->
                if (exists) {
                    Mono.empty()
                } else {
                    val newAuction = Auction(
                        nftId = request.nftId,
                        address = request.address,
                        createdDate = request.createdDate.toInstant().toEpochMilli(),
                        endDate = request.endDate.toInstant().toEpochMilli(),
                        statusType = StatusType.RESERVATION, // 아직 auction 시작전
                        startingPrice = request.startingPrice,
                        tokenType = request.tokenType,
                    )
                    auctionRepository.save(newAuction)
                        .doOnSuccess {
                            // kafkaProducer.sendListing(savedListing).subscribe()
                        }
                }
            }
    }
}