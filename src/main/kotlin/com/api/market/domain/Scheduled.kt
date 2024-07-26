package com.api.market.domain

import com.api.market.enums.StatusType
import com.api.market.enums.TokenType

interface ScheduleEntity {
    val id: Long?
    val nftId: Long
    val statusType: StatusType
    val createdDate: Long
    val endDate: Long
    val tokenType: TokenType
    val address: String
    fun updateStatus(statusType: StatusType): ScheduleEntity
}