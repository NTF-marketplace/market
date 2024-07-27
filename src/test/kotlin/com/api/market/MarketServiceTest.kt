package com.api.market

import com.api.market.controller.dto.request.AuctionCreateRequest
import com.api.market.controller.dto.request.ListingCreateRequest
import com.api.market.controller.dto.response.ListingResponse
import com.api.market.domain.listing.Listing
import com.api.market.domain.listing.ListingRepository
import com.api.market.enums.StatusType
import com.api.market.enums.TokenType
import com.api.market.event.ListingUpdatedEvent
import com.api.market.kafka.KafkaProducer
import com.api.market.rabbitMQ.RabbitMQSender
import com.api.market.service.AuctionService
import com.api.market.service.ListingService
import com.api.market.service.WalletApiService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.concurrent.CountDownLatch

@SpringBootTest
@ActiveProfiles("local")
class MarketServiceTest(
    @Autowired private val listingService: ListingService,
    @Autowired private val listingRepository: ListingRepository,
    @Autowired private val eventPublisher: ApplicationEventPublisher,
    @Autowired private val kafkaProducer: KafkaProducer,
    @Autowired private val walletApiService: WalletApiService,
    @Autowired private val rabbitMQSender: RabbitMQSender,
    @Autowired private val auctionService: AuctionService,
) {


    @Test
    fun createListing() {
        val latch = CountDownLatch(1000)

        for (i in 0 until 1000) {
            val request = Listing(
                nftId = 1L + i,
                address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
                createdDate = System.currentTimeMillis() + (i * 10 * 1000),
                endDate = System.currentTimeMillis() + (i * 30 * 1000),
                price = BigDecimal(3.8),
                tokenType = TokenType.MATIC,
                statusType = StatusType.RESERVATION
            )

            Mono.fromCallable {
                kafkaProducer.sendScheduleEntity("listing-events",request).block()
                latch.countDown()
            }
                .subscribeOn(Schedulers.parallel())
                .subscribe()
        }

        latch.await() // 모든 요청이 완료될 때까지 대기
    }

    @Test
    fun createAndCancelListings() {
        val now = ZonedDateTime.now()
        val listings = listOf(
            ListingCreateRequest(
                nftId = 3L,
                address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
                createdDate = now.plusSeconds(40),
                endDate = now.plusDays(100),
                price = BigDecimal("1.23"),
                tokenType = TokenType.MATIC
            )
        )

        val createdListings = listings.map { request ->
            listingService.saveListing(request)
        }.map { it.block() }

        Thread.sleep(10000)

        val cancelThread = Thread {
            createdListings.forEach { listing ->
                listing?.let {
                    runBlocking {
                        listingService.cancel(it.id!!).block()
                        println("Cancelled listing with ID: ${it.id}")
                    }
                }
            }
        }

        cancelThread.start()

        Thread.sleep(360000)

        cancelThread.join()
    }

    @Test
    fun createMultipleListings() {
        val now = ZonedDateTime.now()
        val listings = listOf(
            ListingCreateRequest(
                nftId = 2L,
                address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
                createdDate = now.plusSeconds(20),
                endDate = now.plusSeconds(40),
                price = BigDecimal("1.23"),
                tokenType = TokenType.MATIC
            ),

        )

        val createdListings = listings.map { request ->
            listingService.saveListing(request)
        }.map { it.block() }

        Thread.sleep(360000)
    }

    @Test
    fun createMultipleAuction() {
        val now = ZonedDateTime.now()
        val auction = listOf(
            AuctionCreateRequest(
                nftId = 2L,
                address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
                createdDate = now.plusSeconds(20),
                endDate = now.plusSeconds(40),
                startingPrice =  BigDecimal("1.23"),
                tokenType = TokenType.MATIC
            ),

            )

        val createdListings = auction.map { request ->
            auctionService.saveAuction(request)
        }.map { it.block() }

        Thread.sleep(360000)
    }


    // @Test
    // fun cancel() {
    //     listingService.cancel(4L)
    // }


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
        statusType = this.statusType,
        tokenType = this.tokenType
    )

    @Test
    fun hello () {
        val res = walletApiService.getAccountNftByAddress1(wallet = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867", nftId = 1L).block()
        println("res : s" + res.toString())
    }

}