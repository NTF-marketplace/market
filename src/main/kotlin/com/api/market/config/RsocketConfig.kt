package com.api.market.config

import com.api.market.domain.offer.Offer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.cbor.Jackson2CborDecoder
import org.springframework.http.codec.cbor.Jackson2CborEncoder
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.web.util.pattern.PathPatternRouteMatcher

@Configuration
class RsocketConfig {

    @Bean
    fun rSocketMessageHandler(rSocketStrategies: RSocketStrategies): RSocketMessageHandler {
        val handler = RSocketMessageHandler()
        handler.rSocketStrategies = rSocketStrategies
        return handler
    }

    @Bean
    fun rSocketStrategies(): RSocketStrategies {
        val objectMapper = jacksonObjectMapper()
        objectMapper.registerSubtypes(Offer::class.java) // Offer 클래스를 Jackson이 인식하도록 설정

        return RSocketStrategies.builder()
            .encoders { encoders ->
                encoders.add(Jackson2JsonEncoder(objectMapper)) // JSON 인코더 사용
            }
            .decoders { decoders ->
                decoders.add(Jackson2JsonDecoder(objectMapper)) // JSON 디코더 사용
            }
            .routeMatcher(PathPatternRouteMatcher())
            .build()
    }
}