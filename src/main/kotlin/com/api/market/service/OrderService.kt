package com.api.market.service

import com.api.market.controller.dto.request.OrderCreateRequest
import com.api.market.domain.listing.ListingRepository
import com.api.market.domain.orders.Orders
import com.api.market.domain.orders.repository.OrdersRepository
import com.api.market.enums.OrderStatusType
import com.api.market.enums.StatusType
import com.api.market.kafka.KafkaProducer
import com.api.market.service.dto.LedgerRequest
import com.api.market.util.Utils.toChainTypes
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class OrderService(
    private val ordersRepository: OrdersRepository,
    private val listingRepository: ListingRepository,
    private val kafkaProducer: KafkaProducer,
    private val walletApiService: WalletApiService,
) {
    // 주문생성(pending) -> 응답후 ->  Ledger 서비스에서 체결로직 완료되면 -> market서비스에서 상태값 변경

    fun create(address: String, request: OrderCreateRequest): Mono<Void> {
        return listingRepository.findByIdAndStatusType(request.listingId,StatusType.ACTIVED)
            .switchIfEmpty(Mono.error(IllegalArgumentException("Listing not found")))
            .flatMap { listing ->
                walletApiService.validAccountBalanceByAddress(address, listing.chainType, listing.price)
                    .flatMap { hasBalance ->
                        if (hasBalance) {
                            ordersRepository.save(
                                Orders(
                                    address = address,
                                    listingId = request.listingId,
                                    statusType = OrderStatusType.PENDING
                                )
                            ).flatMap { order ->
                                kafkaProducer.sendOrderToLedgerService(
                                    LedgerRequest(
                                        nftId = listing.nftId,
                                        address = listing.address,
                                        price = listing.price,
                                        chainType = listing.chainType,
                                        orderAddress = order.address
                                    )
                                )
                            }
                        } else {
                            Mono.error(IllegalArgumentException("Insufficient balance"))
                        }
                    }
            }.then()
    }

}