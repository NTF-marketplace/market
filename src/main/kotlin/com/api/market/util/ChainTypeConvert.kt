package com.api.market.util

import com.api.market.enums.ChainType
import com.api.market.enums.StatusType

import com.api.market.enums.TokenType
import org.springframework.data.r2dbc.convert.EnumWriteSupport

data class ChainTypeConvert<T: Enum<T>>(private val enumType: Class<T>): EnumWriteSupport<ChainType>()

data class TokenTypeConvert<T: Enum<T>>(private val enumType: Class<T>): EnumWriteSupport<TokenType>()


data class StatusTypeConvert<T: Enum<T>>(private val enumType: Class<T>): EnumWriteSupport<StatusType>()