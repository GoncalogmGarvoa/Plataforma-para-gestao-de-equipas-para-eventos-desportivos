package pt.arbitros.arbnet.domain.users

import java.time.LocalDate

data class  User(
    val id: Int,
    val phoneNumber: String,
    val address: String,
    val name: String,
    val email: String,
    val passwordValidation: PasswordValidationInfo,
    val birthDate: LocalDate,
    val iban: String,
    val userStatus: UserStatus,
)

enum class UserStatus(
    val status: String,
) {
    ACTIVE("active"),
    INACTIVE("inactive"),
}


data class UsersIdName(
    val id: Int,
    val name: String,
)