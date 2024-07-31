package com.api.market.service

import com.api.market.controller.dto.request.OrderCreateRequest
import com.api.market.domain.listing.ListingRepository
import com.api.market.domain.orders.Orders
import com.api.market.domain.orders.repository.OrdersRepository
import com.api.market.enums.OrderStatusType
import com.api.market.enums.OrderType
import com.api.market.enums.StatusType
import com.api.market.kafka.KafkaProducer
import com.api.market.service.dto.LedgerRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class OrderService(
    private val ordersRepository: OrdersRepository,
    private val listingRepository: ListingRepository,
    private val kafkaProducer: KafkaProducer,
    private val walletApiService: WalletApiService,
) {
    // 주문생성(pending) -> 응답후 ->  Ledger 서비스에서 체결로직 완료되면 -> market서비스에서 상태값 변경
    // 만약 wallet에서 검증이 실패하면 fail 뜰텐데 그럼 재주문 로작을 만들어야되는거 아닌가?
    // Pending 된 걸 다시 배치로 실행시켜야 되는건가?
    fun create(address: String, request: OrderCreateRequest): Mono<Void> {
        // 여기서 분기처리를 하는게 좋겠다
        // listing(price) or offer(lastPrice)

        return listingRepository.findByIdAndStatusType(request.listingId,StatusType.ACTIVED)
            .switchIfEmpty(Mono.error(IllegalArgumentException("Listing not found")))
            .flatMap { listing ->
                walletApiService.validAccountBalanceByAddress(address, listing.chainType, listing.price)
                    .flatMap { hasBalance ->
                        if (hasBalance) {
                            ordersRepository.save(
                                Orders(
                                    address = address,
                                    orderableId = request.listingId,
                                    orderType = OrderType.LISTING,
                                    statusType = OrderStatusType.PENDING
                                )
                            ).flatMap { order ->

                                // 여기서 분기처리 해야겠지?
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