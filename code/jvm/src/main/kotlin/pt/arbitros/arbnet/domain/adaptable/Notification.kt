package pt.arbitros.arbnet.domain.adaptable

import java.time.LocalDate

class Notification (
    val id: Int,
    val userId: Int,
    val message: String,
    val createdAt: LocalDate,
    val readStatus: Boolean
)
