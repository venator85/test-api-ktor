package com.example.model

import kotlinx.serialization.Serializable

@Serializable
class HomeResponse(
    val user: String,
    val value: Long
)