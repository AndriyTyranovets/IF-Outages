package com.github.andriytyranovets.ifoutages

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.github.andriytyranovets.ifoutages.datastore.DataStoreRepository

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "appdata"
)
class IFOutagesApplication: Application() {
    lateinit var dataStoreRepository: DataStoreRepository
    override fun onCreate() {
        super.onCreate()
        dataStoreRepository = DataStoreRepository(dataStore)
    }
}