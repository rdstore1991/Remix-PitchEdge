package com.example.data

import java.util.concurrent.ConcurrentHashMap

class MemoryTtlCache {
    private data class CacheEntry(
        val data: Any,
        val expiresAtMillis: Long
    )

    private val store = ConcurrentHashMap<String, CacheEntry>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String): T? {
        val entry = store[key] ?: return null
        if (System.currentTimeMillis() > entry.expiresAtMillis) {
            store.remove(key)
            return null
        }
        return entry.data as? T
    }

    fun set(key: String, data: Any, ttlSeconds: Long) {
        store[key] = CacheEntry(
            data = data,
            expiresAtMillis = System.currentTimeMillis() + ttlSeconds * 1000L
        )
    }
}
