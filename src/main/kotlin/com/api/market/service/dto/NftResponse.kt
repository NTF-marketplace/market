package com.api.market.service.dto

import com.api.market.domain.nft.Nft
import com.api.market.enums.ChainType

data class NftResponse(
    val id : Long,
    val tokenId: String,
    val tokenAddress: String,
    val chainType: ChainType,
){
    companion object{
        fun NftResponse.toEntity() = Nft(
            id = this.id,
            tokenId = this.tokenId,
            tokenAddress = this.tokenAddress,
            chainType = this.chainType,
        )
    }
}

