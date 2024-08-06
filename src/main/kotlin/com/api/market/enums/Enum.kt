package com.api.market.enums

enum class ChainType {
    ETHEREUM_MAINNET,
    LINEA_MAINNET,
    LINEA_SEPOLIA,
    POLYGON_MAINNET,
    ETHEREUM_HOLESKY,
    ETHEREUM_SEPOLIA,
    POLYGON_AMOY,
}

enum class TokenType { MATIC, ETH }

enum class StatusType { RESERVATION, ACTIVED, RESERVATION_CANCEL, CANCEL, EXPIRED, LEDGER }

enum class OrderStatusType { PENDING,FAILED,CANCELD,COMPLETED }

enum class OrderType { LISTING, AUCTION }

// enum class AuctionStatusType { RESERVATION, AUCTION, RESERVATION_CANCEL, CANCEL, EXPIRED, LEDGER }