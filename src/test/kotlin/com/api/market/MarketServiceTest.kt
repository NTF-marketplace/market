package com.api.market

import com.api.market.controller.dto.request.ListingCreateRequest
import com.api.market.controller.dto.request.ListingUpdateRequest
import com.api.market.controller.dto.response.ListingResponse
import com.api.market.domain.listing.Listing
import com.api.market.domain.listing.ListingRepository
import com.api.market.enums.TokenType
import com.api.market.event.ListingUpdatedEvent
import com.api.market.kafka.KafkaProducer
import com.api.market.rabbitMQ.RabbitMQSender
import com.api.market.service.ListingService
import com.api.market.service.WalletApiService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.time.Duration

@SpringBootTest
@ActiveProfiles("local")
class MarketServiceTest(
    @Autowired private val listingService: ListingService,
    @Autowired private val listingRepository: ListingRepository,
    @Autowired private val eventPublisher: ApplicationEventPublisher,
    @Autowired private val kafkaProducer: KafkaProducer,
    @Autowired private val walletApiService: WalletApiService,
    @Autowired private val rabbitMQSender: RabbitMQSender,
) {

    // @Test
    // fun createListing() {
    //
    //     val request = ListingCreateRequest(
    //         nftId = 3L,
    //         address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
    //         endDate = ZonedDateTime.now().plusDays(3),
    //         price = 0.23,
    //         tokenType =  TokenType.MATIC
    //     )
    //     listingService.create(request).block()
    // }



    @Test
    fun createMultipleListings() {
        val now = ZonedDateTime.now()
        val listings = listOf(
            ListingCreateRequest(
                nftId = 2L,
                address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
                createdDate = now.plusSeconds(20),
                endDate = now.plusDays(3),
                price = BigDecimal("1.23"),
                tokenType = TokenType.MATIC
            )
//            ListingCreateRequest(
//                nftId = 2L,
//                address = "0x02c83b5bb3f77f324d62d53e829bc172a6a72868",
//                createdDate = now.plusSeconds(20),
//                endDate = now.plusSeconds(60),
//                price = BigDecimal("2.34"),
//                tokenType = TokenType.MATIC
//            ),
//            ListingCreateRequest(
//                nftId = 3L,
//                address = "0x03d94c6cc4f435d72d53e829bc172a6a72869",
//                createdDate = now.plusSeconds(30),
//                endDate = now.plusDays(2),
//                price = BigDecimal("3.45"),
//                tokenType = TokenType.MATIC
//            ),
//            ListingCreateRequest(
//                nftId = 4L,
//                address = "0x03d94c6cc4f435d72d53e829bc172a6a72869",
//                createdDate = now.plusSeconds(40),
//                endDate = now.plusDays(3),
//                price = BigDecimal("3.45"),
//                tokenType = TokenType.MATIC
//            ),
//            ListingCreateRequest(
//                nftId = 5L,
//                address = "0x03d94c6cc4f435d72d53e829bc172a6a72869",
//                createdDate = now.plusSeconds(40),
//                endDate = now.plusDays(3),
//                price = BigDecimal("3.45"),
//                tokenType = TokenType.MATIC
//            ),
//            ListingCreateRequest(
//                nftId = 6L,
//                address = "0x03d94c6cc4f435d72d53e829bc172a6a72869",
//                createdDate = now.plusMinutes(30),
//                endDate = now.plusDays(3),
//                price = BigDecimal("3.45"),
//                tokenType = TokenType.MATIC
//            ),
//            ListingCreateRequest(
//                nftId = 7L,
//                address = "0x03d94c6cc4f435d72d53e829bc172a6a72869",
//                createdDate = now.plusMinutes(20),
//                endDate = now.plusSeconds(50),
//                price = BigDecimal("3.45"),
//                tokenType = TokenType.MATIC
//            )
//
        )

        // 비동기로 모든 Listing 생성
        val createdListings = listings.map { request ->
            listingService.saveListing(request)
        }.map { it.block() }

        Thread.sleep(360000)
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
        createdDateTime = this.createdDate,
        endDateTime =  this.endDate,
        price = this.price,
        active = false,
        tokenType = this.tokenType
    )

//    @Test
//    fun update() {
//        val request = ListingUpdateRequest(
//            endDate = ZonedDateTime.now(),
//            price = 0.24,
//            tokenType = TokenType.MATIC
//        )
//        listingService.update(4,request).block()
//    }

    // @Test
    // fun kafkaTest() {
    //     val message  = "hello kafka"
    //     kafkaProducer.sendMessage("listing-events",message)
    // }

    @Test
    fun hello () {
        val res = walletApiService.getAccountNftByAddress1(wallet = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867", nftId = 1L).block()
        println("res : s" + res.toString())
    }

}