package pt.arbitros.arbnet.http.model.users

import java.time.LocalDate


data class UserCategoryHistoryOutputModel(
    val categoryId: Int,
    val categoryName: String,
    val startDate: LocalDate,
    val endDate: LocalDate?
)

