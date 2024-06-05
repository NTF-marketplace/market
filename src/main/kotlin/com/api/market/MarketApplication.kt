package com.api.market

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class MarketApplication

fun main(args: Array<String>) {
    runApplication<MarketApplication>(*args)
}
