package com.example.myapplication.entities

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ScopeStatus(
    var serialNo: String? = null,
    var status: String? = null,
    var nurse: String? = null,
    var assistant: String? = null
)

