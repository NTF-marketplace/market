package com.api.market

import com.api.market.controller.dto.request.ListingCreateRequest
import com.api.market.controller.dto.request.ListingUpdateRequest
import com.api.market.controller.dto.response.ListingResponse
import com.api.market.domain.listing.Listing
import com.api.market.domain.listing.ListingRepository
import com.api.market.enums.TokenType
import com.api.market.event.ListingUpdatedEvent
import com.api.market.service.ListingService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import java.time.ZonedDateTime

@SpringBootTest
class MarketServiceTest(
    @Autowired private val listingService: ListingService,
    @Autowired private val listingRepository: ListingRepository,
    @Autowired private val eventPublisher: ApplicationEventPublisher,
) {

    @Test
    fun createListing() {

        val request = ListingCreateRequest(
            nftId = 1L,
            address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
            endDate = ZonedDateTime.now().plusDays(3),
            price = 0.23,
            tokenType =  TokenType.MATIC
        )
        listingService.create(request).block()
    }

    @Test
    fun listingSend() {
        val res =listingRepository.findById(4).block()
        eventPublisher.publishEvent(ListingUpdatedEvent(this,res!!.toResponse()))
        Thread.sleep(100000)
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

    @Test
    fun update() {
        val request = ListingUpdateRequest(
            endDate = ZonedDateTime.now(),
            price = 0.26,
            tokenType = TokenType.BTC
        )
        listingService.update(4,request).block()
    }

}