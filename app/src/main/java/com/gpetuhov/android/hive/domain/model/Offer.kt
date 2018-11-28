package com.gpetuhov.android.hive.domain.model

// This is what the user offers to the world
data class Offer(
    var title: String,
    var description: String,
    var price: Double,
    var isFree: Boolean,
    var isActive: Boolean
)