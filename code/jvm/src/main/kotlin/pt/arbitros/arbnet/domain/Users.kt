package pt.arbitros.arbnet.domain

import java.time.LocalDate

data class Users(
    val id: Int,
    val phoneNumber: String,
    val address: String,
    val name: String,
    val email: String,
    val password: String,
    val birthDate: LocalDate,
    val iban: String,
)

enum class UserStatus(
    val status: String,
) {
    ACTIVE("active"),
    INACTIVE("inactive"),
}
