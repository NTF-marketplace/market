package com.api.market.service

import com.api.market.controller.dto.request.AuctionCreateRequest
import com.api.market.domain.auction.AuctionRepository
import com.api.market.domain.auction.Auction
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
        TODO()
        // return auctionRepository.existsByNftIdAndAddressAndStatusType(request.nftId, request.address, request.)
        //     .flatMap { exists ->
        //         if (exists) {
        //             Mono.empty()
        //         } else {
        //             val newListing = Listing(
        //                 nftId = request.nftId,
        //                 address = request.address,
        //                 createdDate = request.createdDate.toInstant().toEpochMilli(),
        //                 endDate = request.endDate.toInstant().toEpochMilli(),
        //                 statusType = StatusType.RESERVATION, // 아직 리스팅 시작전
        //                 price = request.startingPrice,
        //                 tokenType = request.tokenType
        //             )
        //             auctionRepository.save(newListing)
        //                 .doOnSuccess { savedListing ->
        //                     // kafkaProducer.sendListing(savedListing).subscribe()
        //                 }
        //         }
        //     }
    }
}