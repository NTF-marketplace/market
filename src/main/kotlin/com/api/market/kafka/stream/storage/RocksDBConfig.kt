package com.api.market.kafka.stream.storage

import org.apache.kafka.streams.state.RocksDBConfigSetter
import org.rocksdb.Options

class RocksDBConfig : RocksDBConfigSetter {
    override fun setConfig(name: String, options: Options, configs: MutableMap<String, Any>) {
        options.setTtl(3600 * 24 * 30)
    }

    override fun close(name: String, options: Options) {

    }
}