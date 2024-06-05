package com.api.market.util

import com.api.market.enums.ChainType
import org.springframework.data.r2dbc.convert.EnumWriteSupport

data class ChainTypeConvert<T: Enum<T>>(private val enumType: Class<T>): EnumWriteSupport<ChainType>()
