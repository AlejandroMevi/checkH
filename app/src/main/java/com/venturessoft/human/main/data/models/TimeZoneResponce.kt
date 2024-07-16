package com.venturessoft.human.main.data.models

data class TimeZoneResponce(
    val nextAbbreviation: Any,
    val dst: String,
    val formatted: String,
    val regionName: String,
    val message: String,
    val abbreviation: String,
    val zoneEnd: Any,
    val cityName: String,
    val countryCode: String,
    val gmtOffset: Int,
    val zoneStart: Int,
    val countryName: String,
    val zoneName: String,
    val status: String,
    val timestamp: Int
)

