package pt.arbitros.arbnet.http.model.users

import pt.arbitros.arbnet.domain.users.PasswordValidationInfo

class UserOutputPassValModel(
    val id: Int,
    val phoneNumber: String,
    val address: String,
    val name: String,
    val email: String,
    val birthDate: String,
    val iban: String,
    val passwordValidation: PasswordValidationInfo,
    val status: String,
)