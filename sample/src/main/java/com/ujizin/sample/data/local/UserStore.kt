package com.ujizin.sample.data.local

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

interface UserStore {
    fun getUser(): Flow<User>
    suspend fun updateUser(user: User)
}

internal class UserStoreImpl(
    context: Context,
    private val serializer: Json,
) : UserStore {

    private val dataStore: DataStore<Preferences>

    private val userKey = stringPreferencesKey(USER_KEY)

    init {
        dataStore = context.dataStore
    }

    @WorkerThread
    override fun getUser(): Flow<User> = dataStore.data.map { preferences ->
        val user = preferences[userKey] ?: return@map User.Default
        serializer.decodeFromString(user)
    }

    @WorkerThread
    override suspend fun updateUser(user: User) {
        dataStore.edit { preferences -> preferences[userKey] = serializer.encodeToString(user) }
    }

    companion object {
        private const val USER_KEY = "user"
    }
}