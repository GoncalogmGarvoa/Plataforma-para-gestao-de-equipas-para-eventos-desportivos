package pt.arbitros.arbnet.domain.users

import org.springframework.stereotype.Component
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.Success
import pt.arbitros.arbnet.services.UsersService

@Component
class RequestTokenProcessor(
    val usersService: UsersService,
) {
    fun processAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedUser? {
        if (authorizationValue == null) {
            return null
        }
        val parts = authorizationValue.trim().split(" ")
        if (parts.size != 2) {
            return null
        }
        if (parts[0].lowercase() != SCHEME) {
            return null
        }
//        return usersService.getUserByToken(parts[1])?.let {
//            AuthenticatedUser(
//                it,
//                parts[1],
//            )
//        }
        val result = usersService.getUserByToken(parts[1]) ?: return null //TODO check tomas

        return when (result) {
            is Failure -> null
            is Success -> AuthenticatedUser(result.value, parts[1])
        }
    }

    companion object {
        const val SCHEME = "bearer"
    }
}

// data class Users(
//    val id: Int,
//    val phoneNumber: String,
//    val address: String,
//    val name: String,
//    val email: String,
//    val password: PasswordValidationInfo,
//    val birthDate: LocalDate,
//    val iban: String,
//    val passwordValidation: PasswordValidationInfo,
// )
