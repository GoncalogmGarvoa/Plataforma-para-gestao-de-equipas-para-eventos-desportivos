package pt.arbitros.arbnet.http

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.http.model.*
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.UsersService

typealias userEither = Either<Int, Users>

@RestController
class UsersController(
    private val usersService: UsersService,
) {
    @GetMapping(Uris.UsersUris.GET_BY_ID)
    fun getUserById(
        @PathVariable id: Int,
    ): ResponseEntity<UserOutputModel> {
        val aux = usersService.getUserById(id)
        return ResponseEntity.ok(
            UserOutputModel(
                id = aux.id,
                phoneNumber = aux.phoneNumber,
                address = aux.address,
                roles = aux.roles.split(Regex("\\s*,\\s*")).filter { it.isNotBlank() },
                name = aux.name,
                email = aux.email,
                birthDate = aux.birthDate.toString(),
                iban = aux.iban,
            ),
        )
    }

    @GetMapping(Uris.UsersUris.GET_BY_EMAIL)
    fun getUserByEmail(
        @RequestBody email: UsersEmailInput,
    ): ResponseEntity<UserOutputModel> {
        val aux = usersService.getUserByEmail(email.email)
        return ResponseEntity.ok(
            UserOutputModel(
                id = aux.id,
                phoneNumber = aux.phoneNumber,
                address = aux.address,
                roles = aux.roles.split(Regex("\\s*,\\s*")).filter { it.isNotBlank() },
                name = aux.name,
                email = aux.email,
                birthDate = aux.birthDate.toString(),
                iban = aux.iban,
            ),
        )
    }

    // TODO:check if i have to use a model class for response
    @PostMapping(Uris.UsersUris.CREATE_USER)
    fun createUser(
        @RequestBody user: UserInputModel,
    ): ResponseEntity<Int> =
        ResponseEntity.ok(
            usersService.createUser(
                user.name,
                user.phoneNumber,
                user.address,
                user.email,
                user.password,
                user.birthDate,
                user.iban,
            ),
        )

    // TODO: needs to check with token if its the same user being changed
    @PostMapping(Uris.UsersUris.UPDATE_USER)
    fun updateUser(
        @RequestBody user: UserUpdateInputModel,
    ): ResponseEntity<Boolean> =
        ResponseEntity.ok(
            usersService.updateUser(
                user.name,
                user.phoneNumber,
                user.address,
                user.email,
                user.password,
                user.birthDate,
                user.iban,
            ),
        )

    @PostMapping(Uris.UsersUris.USER_ROLES)
    fun updateRoles(
        @RequestBody user: UserRolesUpdateInputModel,
    ): ResponseEntity<Boolean> =
        ResponseEntity.ok(
            usersService.updateRoles(
                user.userId,
                user.roles,
            ),
        )
}
