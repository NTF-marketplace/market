package com.api.market.service

import com.api.market.controller.dto.request.ListingRequest
import com.api.market.domain.listing.Listing
import com.api.market.domain.listing.ListingRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.ZoneOffset

@Service
class ListingService(
    private val walletApiService: WalletApiService,
    private val listingRepository: ListingRepository,
) {


    fun listing(request: ListingRequest) : Mono<Listing> {
         return walletApiService.getAccountNftByAddress(request.address).filter { it == request.nftId }
             .singleOrEmpty()
             .flatMap {
                 saveListing(request)
             }.switchIfEmpty(Mono.error(IllegalArgumentException("Invalid NFT ID or NFT ID not found")))

    }

    fun saveListing(request:ListingRequest): Mono<Listing>{
        return listingRepository.save(
            Listing(
                nftId = request.nftId,
                address = request.address,
                endDate = request.endDate.toInstant(ZoneOffset.UTC).toEpochMilli(),
                isActive = true,
                price = request.price
            )
        )
    }


}