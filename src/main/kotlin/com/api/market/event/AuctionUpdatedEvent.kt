package com.api.market.event

import com.api.market.controller.dto.response.AuctionResponse
import com.api.market.controller.dto.response.ListingResponse
import org.springframework.context.ApplicationEvent

data class AuctionUpdatedEvent(val eventSource: Any, val auction: AuctionResponse): ApplicationEvent(eventSource)
