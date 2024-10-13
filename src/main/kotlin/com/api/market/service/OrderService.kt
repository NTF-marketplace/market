package com.api.market.service

import com.api.market.controller.dto.request.OrderCreateRequest
import com.api.market.controller.dto.response.OrdersResponse
import com.api.market.controller.dto.response.OrdersResponse.Companion.toResponse
import com.api.market.domain.auction.Auction
import com.api.market.domain.listing.ListingRepository
import com.api.market.domain.orders.Orders
import com.api.market.domain.orders.repository.OrdersRepository
import com.api.market.enums.ChainType
import com.api.market.enums.OrderStatusType
import com.api.market.enums.OrderType
import com.api.market.enums.StatusType
import com.api.market.kafka.KafkaProducer
import com.api.market.service.dto.LedgerRequest
import com.api.market.service.dto.LedgerStatusRequest
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class OrderService(
    private val ordersRepository: OrdersRepository,
    private val listingRepository: ListingRepository,
    private val kafkaProducer: KafkaProducer,
    private val offerService: OfferService,
    private val listingService: ListingService,
    @Lazy private val auctionService: AuctionService,
) {

    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    fun orderHistoryByNftId(nftId: Long): Flux<OrdersResponse> {
        val listingIdsMono = listingService.getListingByNftId(nftId, StatusType.LEDGER)
            .map { it.id!! }
            .collectList()

        val auctionIdsMono = auctionService.getAuctionByNfId(nftId, StatusType.LEDGER)
            .map { it.id!! }
            .collectList()

        return listingIdsMono.zipWith(auctionIdsMono)
            .flatMapMany { tuple ->
                val listingIds = tuple.t1
                val auctionIds = tuple.t2

                val listingOrders = ordersRepository.findAllByOrderableIdInAndOrderTypeAndOrderStatusType(
                    listingIds, OrderType.LISTING, OrderStatusType.COMPLETED
                )

                val auctionOrders = ordersRepository.findAllByOrderableIdInAndOrderTypeAndOrderStatusType(
                    auctionIds, OrderType.AUCTION, OrderStatusType.COMPLETED
                )

                listingOrders.concatWith(auctionOrders)
            }
            .sort(Comparator.comparing<Orders, Long> { it.createdAt ?: 0L }.reversed())
            .map { it.toResponse() }
    }




    fun createListingOrder(address: String, request: OrderCreateRequest): Mono<Void> {
        return listingRepository.findByIdAndStatusType(request.orderableId, StatusType.ACTIVED)
            .switchIfEmpty(Mono.error(IllegalArgumentException("Listing not found")))
            .flatMap { listing ->
                createOrder(
                    address = listing.address,
                    orderableId = listing.id!!,
                    orderType = OrderType.LISTING,
                    nftId = listing.nftId,
                    orderAddress = address,
                    price = listing.price,
                    chainType = listing.chainType
                )
            }
    }

    fun createAuctionOrder(auction: Auction): Mono<Void> {
        return offerService.offerPriceDesc(auctionId = auction.id!!)
            .flatMap { offer ->
                createOrder(
                    address = auction.address,
                    orderableId = auction.id,
                    orderType = OrderType.AUCTION,
                    nftId = auction.nftId,
                    orderAddress = offer.address,
                    price = offer.price,
                    chainType = auction.chainType
                )
            }
    }

    private fun createOrder(
        address: String,
        orderableId: Long,
        orderType: OrderType,
        nftId: Long,
        orderAddress: String,
        price: BigDecimal,
        chainType: ChainType
    ): Mono<Void> {
        return ordersRepository.save(
            Orders(
                address = orderAddress,
                orderableId = orderableId,
                orderType = orderType,
                orderStatusType = OrderStatusType.PENDING,
                ledgerPrice = null
            )
        ).flatMap { order ->
            kafkaProducer.sendOrderToLedgerService(
                LedgerRequest(
                    orderId =  order.id!!,
                    nftId = nftId,
                    address = address,
                    price = price,
                    chainType = chainType,
                    orderAddress = order.address
                )
            )
        }
    }

    @Transactional
    fun updateOrderStatus(request: LedgerStatusRequest): Mono<Void> {
        return ordersRepository.findById(request.orderId)
            .flatMap { order ->
                val updatedOrder = order.update(request.status,request.ledgerPrice)
                ordersRepository.save(updatedOrder).then(
                    processStatusUpdate(updatedOrder, request)
                )
            }.then()
    }

    private fun processStatusUpdate(order: Orders, request: LedgerStatusRequest): Mono<Void> {
        return if (request.status == OrderStatusType.COMPLETED) {
            when (order.orderType) {
                OrderType.LISTING -> listingService.updateStatusLeger(order.orderableId)
                OrderType.AUCTION -> auctionService.updateStatusLeger(order.orderableId)
            }
        } else {
            Mono.empty()
        }
    }

}