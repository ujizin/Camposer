package br.com.devlucasyuji.sample.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class DataStore(context: Context) {

    private val dataStore: DataStore<Preferences>
    init {
        dataStore = context.dataStore
    }
}