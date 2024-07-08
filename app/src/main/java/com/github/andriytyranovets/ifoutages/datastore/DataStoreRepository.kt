package com.github.andriytyranovets.ifoutages.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class DataStoreRepository(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val ACCOUNT_NUMBER = stringPreferencesKey("account_number")
        val LAST_UPDATE = stringPreferencesKey("last_update")
        val CACHED_OUTAGES = stringPreferencesKey("cached_outages")
    }

    val accountNumber: Flow<String?> = dataStore.data.map {
        it[ACCOUNT_NUMBER] ?: ""
    }

    val lastUpdate: Flow<LocalDateTime> = dataStore.data.map {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(it[LAST_UPDATE]?.let { it.toLong() } ?: 0),
            ZoneOffset.UTC
        )
    }

    suspend fun saveAccountNumber(accountNumber: String) = save(ACCOUNT_NUMBER, accountNumber)
    suspend fun saveLastUpdate(lastUpdate: LocalDateTime) = save(
        LAST_UPDATE,
        lastUpdate.toInstant(ZoneOffset.UTC).toEpochMilli().toString()
    )

    private suspend fun save(key: Preferences.Key<String>, value: String) {
        dataStore.edit { it[key] = value }
    }
}