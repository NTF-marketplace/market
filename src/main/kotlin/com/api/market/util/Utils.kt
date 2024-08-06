package com.api.market.util

import com.api.market.enums.ChainType
import com.api.market.enums.TokenType

object Utils {

    fun TokenType.toChainTypes(): List<ChainType> {
        return when (this) {
            TokenType.MATIC -> listOf(ChainType.POLYGON_MAINNET, ChainType.POLYGON_AMOY)
            TokenType.ETH -> listOf(ChainType.ETHEREUM_HOLESKY, ChainType.ETHEREUM_MAINNET, ChainType.ETHEREUM_SEPOLIA)
        }
    }
}