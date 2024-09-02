package com.api.market.service.dto

import com.api.market.enums.ChainType
import com.api.market.enums.ContractType
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.math.BigDecimal
data class NftMetadataResponse(
    val id: Long,
    val tokenId: String,
    val tokenAddress: String,
    val contractType: ContractType,
    val chainType: ChainType,
    val nftName: String,
    val collectionName: String,
    val image: String,
    val lastPrice: BigDecimal?,
    val collectionLogo: String?,
)
