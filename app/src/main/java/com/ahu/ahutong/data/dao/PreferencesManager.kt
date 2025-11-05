package com.ahu.ahutong.data.dao

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

object PreferencesKeys {
    val SHOW_QR_CODE = booleanPreferencesKey("show_qr_code")
    val IS_SHOW_ALL_COURSE = booleanPreferencesKey("is_show_all_course")
    val USE_LIQUID_GLASS = booleanPreferencesKey("use_liquid_glass")
}

private val Context.dataStore by preferencesDataStore(name = "user_pref")

class PreferencesManager @Inject constructor(@ApplicationContext private val context: Context) {

    val showQRCode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SHOW_QR_CODE] ?: false
    }

    suspend fun setShowQRCode(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SHOW_QR_CODE] = value
        }
    }

    val isShowAllCourse: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.IS_SHOW_ALL_COURSE] ?: false
    }

    suspend fun setIsShowAllCourse(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_SHOW_ALL_COURSE] = value
        }
    }

    val useLiquidGlass: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.USE_LIQUID_GLASS] ?: true
    }

    suspend fun setUseLiquidGlass(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USE_LIQUID_GLASS] = value
        }
    }

}