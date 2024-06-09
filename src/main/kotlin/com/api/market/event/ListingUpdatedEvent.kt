package com.api.market.event

import com.api.market.controller.dto.response.ListingResponse
import org.springframework.context.ApplicationEvent

data class ListingUpdatedEvent(val eventSource: Any, val listing: ListingResponse): ApplicationEvent(eventSource)
