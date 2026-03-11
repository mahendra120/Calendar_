package com.example.calendar.RoomDatabase

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReminderPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_SELECTED_MINUTES = "selected_minutes"
    }

    // Save selected minutes list
    fun saveSelectedMinutes(minutes: List<Int>) {
        val json = gson.toJson(minutes)
        prefs.edit().putString(KEY_SELECTED_MINUTES, json).apply()
    }

    // Load selected minutes list
    fun loadSelectedMinutes(): MutableList<Int> {
        val json = prefs.getString(KEY_SELECTED_MINUTES, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Int>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    // Clear saved data
    fun clearSelectedMinutes() {
        prefs.edit().remove(KEY_SELECTED_MINUTES).apply()
    }
}