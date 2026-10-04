package com.example.ohmmate.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ohmmate_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        const val MAX_HISTORY_RECORDS = 50

        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_DEFAULT_PARAMS = stringPreferencesKey("default_params")
        private val KEY_DECIMAL_PLACES = intPreferencesKey("decimal_places")
        private val KEY_HAPTIC = booleanPreferencesKey("haptic_feedback")
        private val KEY_SHOW_FORMULA = booleanPreferencesKey("show_formula")
        private val KEY_SHOW_SAFETY = booleanPreferencesKey("show_safety_note")
        private val KEY_AUTO_SCALE = booleanPreferencesKey("auto_scale_units")
        private val KEY_HISTORY_JSON = stringPreferencesKey("history_json")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val themeStr = prefs[KEY_THEME] ?: ThemeMode.SYSTEM.name
            val theme = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.SYSTEM)
            val dynamic = prefs[KEY_DYNAMIC_COLOR] ?: true
            val defParamStr = prefs[KEY_DEFAULT_PARAMS] ?: DefaultParameterPair.VOLTAGE_RESISTANCE.name
            val defParams = runCatching { DefaultParameterPair.valueOf(defParamStr) }.getOrDefault(DefaultParameterPair.VOLTAGE_RESISTANCE)
            val decimals = prefs[KEY_DECIMAL_PLACES] ?: 2
            val haptic = prefs[KEY_HAPTIC] ?: true
            val showFormula = prefs[KEY_SHOW_FORMULA] ?: true
            val showSafety = prefs[KEY_SHOW_SAFETY] ?: true
            val autoScale = prefs[KEY_AUTO_SCALE] ?: false

            AppSettings(
                themeMode = theme,
                dynamicColor = dynamic,
                defaultParameters = defParams,
                decimalPlaces = decimals,
                hapticFeedback = haptic,
                showFormula = showFormula,
                showSafetyNote = showSafety,
                autoScaleUnits = autoScale
            )
        }

    val historyFlow: Flow<List<HistoryItem>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val json = prefs[KEY_HISTORY_JSON]
            HistoryJsonSerializer.deserialize(json)
        }

    suspend fun saveHistory(item: HistoryItem) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_HISTORY_JSON]
            val currentList = HistoryJsonSerializer.deserialize(currentJson).toMutableList()
            // Add new item at index 0
            currentList.add(0, item)
            // Trim to max 50 records
            val trimmedList = if (currentList.size > MAX_HISTORY_RECORDS) {
                currentList.take(MAX_HISTORY_RECORDS)
            } else {
                currentList
            }
            prefs[KEY_HISTORY_JSON] = HistoryJsonSerializer.serialize(trimmedList)
        }
    }

    suspend fun deleteHistoryItem(id: String) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_HISTORY_JSON]
            val currentList = HistoryJsonSerializer.deserialize(currentJson)
            val updatedList = currentList.filterNot { it.id == id }
            prefs[KEY_HISTORY_JSON] = HistoryJsonSerializer.serialize(updatedList)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { prefs ->
            prefs[KEY_HISTORY_JSON] = "[]"
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = mode.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setDefaultParameters(pair: DefaultParameterPair) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_PARAMS] = pair.name
        }
    }

    suspend fun setDecimalPlaces(places: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DECIMAL_PLACES] = places.coerceIn(1, 3)
        }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAPTIC] = enabled
        }
    }

    suspend fun setShowFormula(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_FORMULA] = enabled
        }
    }

    suspend fun setShowSafetyNote(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_SAFETY] = enabled
        }
    }

    suspend fun setAutoScaleUnits(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_SCALE] = enabled
        }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = ThemeMode.SYSTEM.name
            prefs[KEY_DYNAMIC_COLOR] = true
            prefs[KEY_DEFAULT_PARAMS] = DefaultParameterPair.VOLTAGE_RESISTANCE.name
            prefs[KEY_DECIMAL_PLACES] = 2
            prefs[KEY_HAPTIC] = true
            prefs[KEY_SHOW_FORMULA] = true
            prefs[KEY_SHOW_SAFETY] = true
            prefs[KEY_AUTO_SCALE] = false
        }
    }
}
