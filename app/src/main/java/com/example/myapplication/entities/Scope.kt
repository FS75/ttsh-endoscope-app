package com.example.myapplication.entities

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Scope(
    var assistant: String? = null,
    var brand: String? = null,
    var date: String? = null,
    var model: String? = null,
    var nurse: String? = null,
    var serialNo: String? = null,
    var shift: String? = null,
    var status: String? = null,
    var time: String? = null,
    var type: String? = null
)