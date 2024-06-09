package com.api.market.event

import org.springframework.context.ApplicationEvent

data class ListingCanceledEvent(val eventSource: Any, val nftIds: List<Long>): ApplicationEvent(eventSource)
