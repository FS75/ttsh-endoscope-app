package com.example.myapplication.entities

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Employee(
    var id: String? = null,
    var name: String? = null,
    var type: String? = null,
    var email: String? = null
) {}