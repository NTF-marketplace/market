package com.api.market

import com.api.market.controller.dto.request.ListingCreateRequest
import com.api.market.enums.TokenType
import com.api.market.service.ListingService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime

@SpringBootTest
class MarketServiceTest(
    @Autowired private val listingService: ListingService,
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
    fun getListing() {
        val res = listingService.getListingByNftId(1).block()


    }


}