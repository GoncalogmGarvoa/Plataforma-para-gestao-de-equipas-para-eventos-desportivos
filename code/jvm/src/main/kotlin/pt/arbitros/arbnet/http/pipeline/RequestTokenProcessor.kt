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
        return when (val result = usersService.getUserByToken(parts[1])) {
            is Success -> AuthenticatedUser(result.value, parts[1])
            is Failure -> null // Handle failure case, e.g., token not found or invalid
        }

        // TODO also check role
        //val role = usersService.getUserRoleByToken(parts[1]) ?: throw IllegalStateException()
    }

    companion object {
        const val SCHEME = "bearer"
    }
}

