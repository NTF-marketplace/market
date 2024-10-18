package com.api.market.domain.orders

import com.api.market.domain.listing.Listing
import com.api.market.enums.OrderStatusType
import com.api.market.enums.OrderType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table(name ="orders")
data class Orders (
    @Id val id : Long? = null,
    val orderableId: Long,
    val orderType: OrderType,
    val address: String,
    val ledgerPrice: BigDecimal?,
    val createdAt: Long? = System.currentTimeMillis(),
    val orderStatusType: OrderStatusType
){
    fun update(updateStatus: OrderStatusType,ledgerPrice: BigDecimal?): Orders {
        return this.copy(
            orderStatusType = updateStatus,
            ledgerPrice = ledgerPrice
        )
    }
}