package com.api.market

import com.api.market.controller.dto.request.AuctionCreateRequest
import com.api.market.controller.dto.request.ListingCreateRequest
import com.api.market.controller.dto.request.OfferCreateRequest
import com.api.market.controller.dto.request.OrderCreateRequest
import com.api.market.controller.dto.response.ListingResponse
import com.api.market.domain.listing.Listing
import com.api.market.domain.listing.ListingRepository
import com.api.market.enums.ChainType
import com.api.market.enums.OrderType
import com.api.market.enums.StatusType
import com.api.market.kafka.KafkaProducer
import com.api.market.service.AuctionService
import com.api.market.service.ListingService
import com.api.market.service.OfferService
import com.api.market.service.OrderService
import com.api.market.service.dto.LedgerRequest
import com.api.market.service.dto.SaleResponse
import com.api.market.service.external.RedisService
import com.api.market.service.external.WalletApiService
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
    @Autowired private val auctionService: AuctionService,
    @Autowired private val redisService: RedisService,
    @Autowired private val orderService: OrderService,
    @Autowired private val offerService: OfferService,
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
                chainType = ChainType.POLYGON_MAINNET,
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



    //?
    @Test
    fun createAndCancelListings() {
        val now = ZonedDateTime.now()
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val listings = listOf(
            ListingCreateRequest(
                nftId = 10L,
                createdDate = now.plusSeconds(20),
                endDate = now.plusDays(100),
                price = BigDecimal("1.23"),
                chainType = ChainType.POLYGON_MAINNET
            )
        )

        val createdListings = listings.map { request ->
            listingService.saveListing(address,request)
        }.map { it.block() }

        Thread.sleep(40000)

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
    fun createAndOrderListings() {
        val now = ZonedDateTime.now()
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val listings = listOf(
            ListingCreateRequest(
                nftId = 4L,
                createdDate = now.plusSeconds(20),
                endDate = now.plusDays(100),
                price = BigDecimal("1.23"),
                chainType = ChainType.POLYGON_MAINNET
            )
        )

        val createdListings = listings.map { request ->
            listingService.saveListing(address,request)
        }.map { it.block() }

        Thread.sleep(40000)

        val cancelThread = Thread {
            createdListings.forEach { listing ->
                listing?.let {
                    runBlocking {
                        orderService.createListingOrder("0x01b72b4aa3f66f213d62d53e829bc172a6a72868",
                            OrderCreateRequest(orderableId = it.id!!,OrderType.LISTING)
                        ).block()
                        println("ordered listing with ID: ${it.id}")
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
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val auction = listOf(
            AuctionCreateRequest(
                nftId = 1L,
                createdDate = now.plusSeconds(20),
                endDate = now.plusSeconds(50),
                startingPrice =  BigDecimal("1.23"),
                chainType = ChainType.POLYGON_MAINNET
            ),
        )

        Thread.sleep(40000)

        val createdAuctions = auction.map { request ->
            auctionService.create(address,request)
        }.map { it.block() }

        val cancelThread = Thread {
            createdAuctions.forEach { auction ->
                auction?.let {
                    runBlocking {
                        offerService.create("0x01b72b4aa3f66f213d62d53e829bc172a6a72868",
                            OfferCreateRequest(auctionId = it.id!!, BigDecimal(2.8))
                        ).block()
                        println("ordered auction with ID: ${it.id}")
                    }
                }
            }
        }

        cancelThread.start()

        Thread.sleep(360000)

        cancelThread.join()
    }

    @Test
    fun createMultipleAuction() {
        val now = ZonedDateTime.now()
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val auction = listOf(
            AuctionCreateRequest(
                nftId = 5L,
                createdDate = now.plusSeconds(20),
                endDate = now.plusDays(3),
                startingPrice =  BigDecimal("1.23"),
                chainType = ChainType.POLYGON_MAINNET
            ),

            )

        val createdListings = auction.map { request ->
            auctionService.saveAuction(address,request)
        }.map { it.block() }

        Thread.sleep(360000)
    }


    // @Test
    // fun cancel() {
    //     listingService.cancel(4L)
    // }

    private fun Listing.toResponse() = ListingResponse (
        id = this.id!!,
        nftId = this.nftId,
        address = this.address,
        createdDateTime = this.createdDate,
        endDateTime =  this.endDate,
        price = this.price,
        statusType = this.statusType,
        chainType = this.chainType
    )

//     @Test
//     fun hasBalance() {
//         val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
//         val request = OrderCreateRequest(
//             orderableId = 6L,
//             orderType = OrderType.AUCTION,
//         )
//
// //        nftId = listing.nftId,
// //        address = listing.address,
// //        price = listing.price,
// //        chainType = listing.chainType,
// //        orderAddress = order.address
//         orderService.create(address,request).block()
//     }
//

    @Test
    fun createOrderListing() {
        val request = OrderCreateRequest(
            orderableId = 14L,
            orderType = OrderType.LISTING
        )
        orderService.createListingOrder(address =  "0x01b82b4aa3f66f213d62d53e829bc172a6a72867", request).block()

        // val request1 = OrderCreateRequest(
        //     orderableId = 9L,
        //     orderType = OrderType.LISTING
        // )
        // orderService.createListingOrder(address =  "0x01b82b4aa3f66f213d62d53e829bc172a6a72867", request1).block()
        Thread.sleep(380000)

    }

    @Test
    fun redis() {
        val res = redisService.getNft(4L).block()
        println(res.toString())
    }
    @Test
    fun kafka() {
        kafkaProducer
            .sendOrderToLedgerService(LedgerRequest(orderId = 1L, nftId = 3L, address = "asdasdas", price = BigDecimal(1.2), chainType = ChainType.POLYGON_MAINNET, orderAddress = "asdasda"))
            .block()
    }

    @Test
    fun kafka1() {
        kafkaProducer
            .sendSaleStatusService(
                SaleResponse(
                    id = 1L,
                    nftId = 13L,
                    address = "123123123",
                    createdDateTime = System.currentTimeMillis(),
                    endDateTime =System.currentTimeMillis(),
                    statusType = StatusType.ACTIVED,
                    price = BigDecimal(1.2),
                    chainType = ChainType.POLYGON_MAINNET,
                    orderType = OrderType.LISTING

                )
            )
            .block()
    }


    @Test
    fun listing_reservation() {
        val now = ZonedDateTime.now()
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val listings = listOf(
            ListingCreateRequest(
                nftId = 1L,
                createdDate = now.plusSeconds(30),
                endDate = now.plusSeconds(50),
                price = BigDecimal("1.23"),
                chainType = ChainType.POLYGON_MAINNET
            )
        )

        listings.map { request ->
            listingService.saveListing(address,request)
        }.map { it.block() }

        Thread.sleep(36000)

    }
}