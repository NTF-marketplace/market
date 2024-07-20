package com.api.market.scheduel

import com.api.market.service.ListingService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ListingUpdateScheduler(
    private val listingService: ListingService,
) {
//    @Scheduled(fixedRate = 60000)
//    fun batchCancelTask() {
//        val time = Instant.now().toEpochMilli()
//        listingService.batchCancel(time).subscribe()
//    }
}