package com.api.market.service

import com.api.market.domain.listing.ListingRepository
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val walletApiService: WalletApiService,
    private val listingRepository: ListingRepository,
) {

//    fun create(address: String, listingId: Long) {
//        listingRepository.findById(listingId).flatMap {
//            it.price
//        }
//        walletApiService.getAccountByAddress(address)
//    }

}