package pt.arbitros.arbnet.domain

import java.time.LocalDate

data class CategoryDir(
    val refereeId: Int,
    val id: Int,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val categoryId: Int
)
