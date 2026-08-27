package com.cabelas.smokercompanion.data.model

enum class TemperatureUnit {
    FAHRENHEIT,
    CELSIUS;

    fun format(tempF: Int): String {
        return when (this) {
            FAHRENHEIT -> "$tempF°F"
            CELSIUS -> "${((tempF - 32) * 5) / 9}°C"
        }
    }

    fun convertFromF(tempF: Int): Int {
        return when (this) {
            FAHRENHEIT -> tempF
            CELSIUS -> ((tempF - 32) * 5) / 9
        }
    }

    fun convertToF(tempInUnit: Int): Int {
        return when (this) {
            FAHRENHEIT -> tempInUnit
            CELSIUS -> ((tempInUnit * 9) / 5) + 32
        }
    }
}
