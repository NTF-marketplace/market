package com.api.market.service

import com.api.market.controller.dto.request.OrderCreateRequest
import com.api.market.domain.auction.Auction
import com.api.market.domain.auction.AuctionRepository
import com.api.market.domain.listing.ListingRepository
import com.api.market.domain.orders.Orders
import com.api.market.domain.orders.repository.OrdersRepository
import com.api.market.enums.ChainType
import com.api.market.enums.OrderStatusType
import com.api.market.enums.OrderType
import com.api.market.enums.StatusType
import com.api.market.kafka.KafkaProducer
import com.api.market.service.dto.LedgerRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class OrderService(
    private val ordersRepository: OrdersRepository,
    private val listingRepository: ListingRepository,
    private val kafkaProducer: KafkaProducer,
    private val auctionRepository: AuctionRepository,
    private val offerService: OfferService,
) {
    // 주문생성(pending) -> 응답후 ->  Ledger 서비스에서 체결로직 완료되면 -> market서비스에서 상태값 변경
    // 체결 Transfer로 요청하고 transfer success 받으면 ledgerLog 생성
    // transfer log를 만드는것도 나쁘지 않다고 생각함

    fun createListingOrder(address: String, request: OrderCreateRequest): Mono<Void> {
        return listingRepository.findByIdAndStatusType(request.orderableId, StatusType.ACTIVED)
            .switchIfEmpty(Mono.error(IllegalArgumentException("Listing not found")))
            .flatMap { listing ->
                createOrder(
                    address = listing.address,
                    orderableId = listing.id!!,
                    orderType = OrderType.LISTING,
                    nftId = listing.nftId,
                    orderAddress = listing.address,
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
                address = address,
                orderableId = orderableId,
                orderType = orderType,
                statusType = OrderStatusType.PENDING
            )
        ).flatMap { order ->
            kafkaProducer.sendOrderToLedgerService(
                LedgerRequest(
                    nftId = nftId,
                    address = orderAddress,
                    price = price,
                    chainType = chainType,
                    orderAddress = order.address
                )
            )
        }
    }
}