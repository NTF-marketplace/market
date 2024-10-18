package com.api.market.service

import com.api.market.controller.dto.request.AuctionCreateRequest
import com.api.market.domain.auction.AuctionRepository
import com.api.market.domain.auction.Auction
import com.api.market.enums.StatusType
import com.api.market.kafka.KafkaProducer
import com.api.market.service.dto.SaleResponse.Companion.toResponse
import com.api.market.service.external.RedisService
import com.api.market.service.external.WalletApiService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AuctionService(
    private val walletApiService: WalletApiService,
    private val auctionRepository: AuctionRepository,
    private val kafkaProducer: KafkaProducer,
    private val orderService: OrderService,
    private val redisService: RedisService,
) {

    fun getAuctionByNfId(nftId:Long , status: StatusType): Flux<Auction> {
        return auctionRepository.findByNftIdAndStatusType(nftId,status)
    }

    fun findByActivedAuctionId(auctionId: Long): Mono<Auction> {
        return auctionRepository.findByIdAndStatusType(auctionId,StatusType.ACTIVED)
            .switchIfEmpty(
                Mono.error(IllegalArgumentException(""))
            )
    }

    fun create(address: String,request: AuctionCreateRequest) : Mono<Auction> {
        return redisService.getNft(request.nftId)
            .switchIfEmpty(Mono.error(IllegalArgumentException("")))
            .flatMap {
                walletApiService.validNftByAddress(address, request.nftId)
                    .flatMap { nftExists ->
                        if(nftExists) {
                            saveAuction(address,request)
                        } else {
                            Mono.error(IllegalArgumentException("Invalid NFT ID"))
                        }
                    }
                    .doOnSuccess { kafkaProducer.sendSaleStatusService(it.toResponse()).subscribe() }
            }

    }


    fun update(auction: Auction): Mono<Void> {
        return auctionRepository.findById(auction.id!!)
            .map { it.update(auction) }
            .flatMap { auctionRepository.save(it) }
            .doOnSuccess {
                kafkaProducer.sendSaleStatusService(it.toResponse()).subscribe()
            }
            .flatMap {
                if (it.statusType == StatusType.EXPIRED) {
                    orderService.createAuctionOrder(it)
                } else {
                    Mono.empty()
                }
            }
            .then()
    }

    fun saveAuction(address: String,request: AuctionCreateRequest): Mono<Auction> {
        return auctionRepository.existsByNftIdAndAddressAndStatusType(request.nftId, address, StatusType.RESERVATION)
            .flatMap { exists ->
                if (exists) {
                    Mono.empty()
                } else {
                    val newAuction = Auction(
                        nftId = request.nftId,
                        address = address,
                        createdDate = request.createdDate.toInstant().toEpochMilli(),
                        endDate = request.endDate.toInstant().toEpochMilli(),
                        statusType = StatusType.RESERVATION,
                        startingPrice = request.startingPrice,
                        chainType = request.chainType
                    )
                    auctionRepository.save(newAuction)
                        .flatMap { savedAuction ->
                            kafkaProducer.sendScheduleEntity("auction-events", savedAuction)
                                .thenReturn(savedAuction)
                        }
                }
            }
    }

    fun updateStatusLeger(orderId: Long): Mono<Void> {
        return auctionRepository.findById(orderId)
            .flatMap { auction ->
                val updatedEntity = auction.updateStatus(StatusType.LEDGER)
                if (updatedEntity is Auction) {
                    update(updatedEntity).then()
                } else {
                    Mono.error(IllegalStateException("Expected an Auction but got ${updatedEntity::class.simpleName}"))
                }
            }
    }

}