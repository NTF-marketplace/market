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

enum class TokenType { MATIC, ETH, BTC }

enum class StatusType { RESERVATION, LISTING, RESERVATION_CANCEL, CANCEL, EXPIRED, LEDGER, AUCTION }

// enum class AuctionStatusType { RESERVATION, AUCTION, RESERVATION_CANCEL, CANCEL, EXPIRED, LEDGER }