package com.api.market.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "wallet")
data class WalletApiProperties(
    val uri: String
)
