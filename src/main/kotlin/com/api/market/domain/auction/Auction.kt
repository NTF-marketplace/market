package com.api.market.domain.auction

import com.api.market.domain.ScheduleEntity
import com.api.market.enums.StatusType
import com.api.market.enums.TokenType
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal


@JsonSerialize
@JsonDeserialize
@Table("auction")
data class Auction @JsonCreator constructor(
    @JsonProperty("id") @Id override val id : Long? = null,
    @JsonProperty("nftId") override val nftId: Long,
    @JsonProperty("address") override val address: String,
    @JsonProperty("createdDate") override val createdDate: Long,
    @JsonProperty("endDate")override val endDate: Long,
    @JsonProperty("statusType")override val statusType: StatusType,
    @JsonProperty("startingPrice") val startingPrice: BigDecimal,
    @JsonProperty("tokenType")override val tokenType: TokenType
): ScheduleEntity {
    override fun updateStatus(statusType: StatusType): ScheduleEntity {
        return this.copy(statusType = statusType)
    }
}
