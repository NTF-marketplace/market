package com.api.market.util

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter

@ReadingConverter
class StringToEnumConverter<T : Enum<T>>(private val enumType: Class<T>) : Converter<String, T> {
    override fun convert(source: String): T {
        return java.lang.Enum.valueOf(enumType, source)
    }
}