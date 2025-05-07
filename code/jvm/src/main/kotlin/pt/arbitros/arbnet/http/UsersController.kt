@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.http.model.*
import pt.arbitros.arbnet.services.*

typealias userEither = Either<Int, Users>

@RestController
class UsersController(
    private val usersService: UsersService,
) {
    @GetMapping(Uris.UsersUris.GET_BY_ID)
    fun getUserById(
        @PathVariable id: Int,
    ): ResponseEntity<*> =
        // UserOutputModel
        when (
            val userInfo = usersService.getUserById(id)

        ) {
            is Success ->
                ResponseEntity.ok(
                    UserOutputModel(
                        id = userInfo.value.id,
                        phoneNumber = userInfo.value.phoneNumber,
                        address = userInfo.value.address,
                        // roles = aux.roles,
                        name = userInfo.value.name,
                        email = userInfo.value.email,
                        birthDate = userInfo.value.birthDate.toString(),
                        iban = userInfo.value.iban,
                    ),
                )
            is Failure ->
                when (userInfo.value) {
                    is UsersError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to get the user")
                }
        }

    @GetMapping(Uris.UsersUris.GET_BY_EMAIL)
    fun getUserByEmail(
        @RequestBody email: UsersEmailInput,
    ): ResponseEntity<*> =
        when (
            val userInfo = usersService.getUserByEmail(email.email)
        ) {
            is Success ->
                ResponseEntity.ok(
                    UserOutputModel(
                        id = userInfo.value.id,
                        phoneNumber = userInfo.value.phoneNumber,
                        address = userInfo.value.address,
                        // roles = aux.roles,
                        name = userInfo.value.name,
                        email = userInfo.value.email,
                        birthDate = userInfo.value.birthDate.toString(),
                        iban = userInfo.value.iban,
                    ),
                )
            is Failure ->
                when (userInfo.value) {
                    is UsersError.EmailNotFound -> Problem.EmailNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to get the user")
                }
        }

    @PostMapping(Uris.UsersUris.CREATE_USER)
    fun createUser(
        @RequestBody user: UserInputModel,
    ): ResponseEntity<*> =
        when (
            val userCreated =
                usersService.createUser(
                    UserInputModel(
                        user.name,
                        user.phoneNumber,
                        user.address,
                        user.email,
                        user.password,
                        user.birthDate,
                        user.iban,
                    ),
                )
        ) {
            is Success -> ResponseEntity.ok(userCreated)
            is Failure ->
                when (userCreated.value) {
                    is UsersError.EmailAlreadyUsed -> Problem.EmailAlreadyUsed.response(HttpStatus.CONFLICT)
                    is UsersError.PhoneNumberAlreadyUsed -> Problem.PhoneNumberAlreadyUsed.response(HttpStatus.CONFLICT)
                    is UsersError.IbanAlreadyUsed -> Problem.IbanAlreadyUsed.response(HttpStatus.CONFLICT)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to create the user")
                }
        }

    // TODO: needs to check with token if its the same user being changed
    @PutMapping(Uris.UsersUris.UPDATE_USER)
    fun updateUser(
        @RequestBody user: UserUpdateInputModel,
    ): ResponseEntity<*> =
        when (
            val userUpdated =
                usersService.updateUser(
                    UserUpdateInputModel(
                        user.id,
                        user.name,
                        user.phoneNumber,
                        user.address,
                        user.email,
                        user.password,
                        user.birthDate,
                        user.iban,
                    ),
                )
        ) {
            is Success -> ResponseEntity.ok(userUpdated)
            is Failure ->
                when (userUpdated.value) {
                    is UsersError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                    is UsersError.EmailAlreadyUsed -> Problem.EmailAlreadyUsed.response(HttpStatus.CONFLICT)
                    is UsersError.PhoneNumberAlreadyUsed -> Problem.PhoneNumberAlreadyUsed.response(HttpStatus.CONFLICT)
                    is UsersError.IbanAlreadyUsed -> Problem.IbanAlreadyUsed.response(HttpStatus.CONFLICT)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to update the user")
                }
        }

    @PutMapping(Uris.UsersUris.USER_ROLES)
    fun updateRoles(
        @RequestBody user: UserRolesUpdateInputModel,
    ): ResponseEntity<*> =

        when (
            val userRolesUpdated =
                usersService.updateUserRoles(
                    user.userId,
                    user.roleId,
                    user.addOrRemove,
                )
        ) {
            is Success -> ResponseEntity.ok(userRolesUpdated)
            is Failure ->
                when (userRolesUpdated.value) {
                    is UsersError.RoleNotFound -> Problem.RoleNotFound.response(HttpStatus.NOT_FOUND)
                    is UsersError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                    is UsersError.UserWithoutRole -> Problem.UserWithoutRole.response(HttpStatus.BAD_REQUEST)
                    is UsersError.UserAlreadyHasRole -> Problem.UserAlreadyHasRole.response(HttpStatus.CONFLICT)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to update the roles")
                }
        }
}
