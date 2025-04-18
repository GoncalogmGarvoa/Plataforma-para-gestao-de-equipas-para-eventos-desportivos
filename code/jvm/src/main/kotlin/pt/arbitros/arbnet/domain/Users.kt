package pt.arbitros.arbnet.domain

import java.time.LocalDate

data class Users(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val birthDate: LocalDate?,
    val iban: String?,
    val roles: String//List<String>
)

