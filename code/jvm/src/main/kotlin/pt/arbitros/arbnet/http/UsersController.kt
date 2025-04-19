package pt.arbitros.arbnet.http

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.http.model.UserInputModel
import pt.arbitros.arbnet.http.model.UserOutputModel
import pt.arbitros.arbnet.http.model.UserUpdateInputModel
import pt.arbitros.arbnet.http.model.UsersEmailInput
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.UsersService

typealias userEither = Either<Int, Users>

@RestController
class UsersController(
    private val usersService: UsersService,
) {
    @GetMapping(Uris.Users.GET_BY_ID)
    fun getUserById(
        @PathVariable id: Int,
    ): UserOutputModel? {
        val aux = usersService.getUserById(id)
        return UserOutputModel(
            id = aux.id,
            roles = aux.roles.split(Regex("\\s*,\\s*")).filter { it.isNotBlank() },
            name = aux.name,
            email = aux.email,
            birthDate = aux.birthDate.toString(),
            iban = aux.iban,
        )
    }

    @GetMapping(Uris.Users.GET_BY_EMAIL)
    fun getUserByEmail(
        @RequestBody email: UsersEmailInput,
    ): UserOutputModel? {
        val aux = usersService.getUserByEmail(email.email)
        return UserOutputModel(
            id = aux.id,
            roles = aux.roles.split(Regex("\\s*,\\s*")).filter { it.isNotBlank() },
            name = aux.name,
            email = aux.email,
            birthDate = aux.birthDate.toString(),
            iban = aux.iban,
        )
    }

    // TODO:check if i have to use a model class
    @PostMapping(Uris.Users.CREATE_USER)
    fun createUser(
        @RequestBody user: UserInputModel,
    ): Int =
        usersService.createUser(
            user.name,
            user.email,
            user.password,
            user.birthDate,
            user.iban,
        )

    // TODO: needs to check with token if its the same user being changed
    @PostMapping(Uris.Users.UPDATE_USER)
    fun updateUser(
        @RequestBody user: UserUpdateInputModel,
    ): Boolean =
        usersService.updateUser(
            user.name,
            user.email,
            user.password,
            user.birthDate,
            user.iban,
        )
}
