package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class BookingWithDetails(
    @Embedded
    val booking: BookingEntity,

    @Relation(
        parentColumn = "ambulance_id",
        entityColumn = "ambulance_id"
    )
    val ambulance: AmbulanceEntity? = null,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val user: UserEntity? = null
)
