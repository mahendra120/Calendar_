package com.example.calendar.RoomDatabase

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object Prefs {

    private const val PREF_NAME = "calendar_prefs"
    private const val KEY_SELECTED_REGIONS = "selected_regions"

    fun saveSelectedRegions(context: Context, regions: Set<String>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_SELECTED_REGIONS, regions).apply()
    }

    fun loadSelectedRegions(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_SELECTED_REGIONS, emptySet()) ?: emptySet()
    }
}


object localAccountPrefs {

    private const val PREF_NAME = "calendar_prefs"
    private const val KEY_LOCAL_ACCOUNT = "local_account_done"

    fun saveLocalAccount(context: Context, value: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LOCAL_ACCOUNT, value)
            .apply()
    }

    fun loadLocalAccount(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_LOCAL_ACCOUNT, false)
    }
}

object CalendarDisplayPrefs {

    private const val PREF = "calendar_display"
    private const val KEY_TEXT = "selected_text"

    fun save(context: Context, text: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TEXT, text)
            .apply()
    }

    fun load(context: Context): String {
        return context
            .getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_TEXT, "local account") ?: "local account"
    }
}


object CalendarPrefs {

    private const val PREF_NAME = "calendar_prefs"
    private const val KEY_SELECTED_CALENDARS = "selected_calendar_ids"

    fun saveSelectedCalendars(
        context: Context,
        ids: Set<Int>
    ) {
        val stringSet = ids.map { it.toString() }.toSet()

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_SELECTED_CALENDARS, stringSet)
            .apply()
    }

    fun loadSelectedCalendars(context: Context): Set<Int> {
        val stringSet =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getStringSet(KEY_SELECTED_CALENDARS, emptySet())

        return stringSet?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }
}

object CalendarColorPrefs {

    const val PREF = "calendar_color_prefs"
    const val KEY_COLOR = "selected_calendar_color"

    fun save(context: Context, color: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_COLOR, color)
            .apply()
    }

    fun load(context: Context): Int {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_COLOR, 0xFFFF9800.toInt()) // default orange
    }
}


object colosave {

    fun saveThemeColor(context: Context, color: Color) {
        val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("theme_color", color.toArgb())
            .apply()
    }

    fun loadThemeColor(context: Context): Color {
        val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val colorInt = prefs.getInt("theme_color", Color.Black.toArgb())
        return Color(colorInt)
    }


}