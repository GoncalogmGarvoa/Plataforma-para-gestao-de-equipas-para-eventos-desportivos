@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.arbitros.arbnet.domain.users.AuthenticatedUser
import pt.arbitros.arbnet.http.model.*
import pt.arbitros.arbnet.services.*

@RestController
class UsersController(
    private val usersService: UsersService,
) {
    @PostMapping(Uris.UsersUris.TOKEN) // equal to logging in
    fun token(
        @RequestBody input: UserCreateTokenInputModel,
    ): ResponseEntity<*> {
        val res = usersService.createToken(input.email, input.password)
        return when (res) {
            is Success ->
                ResponseEntity.ok(UserTokenCreateOutputModel(res.value.tokenValue))

            is Failure ->
                when (res.value) {
                    is UsersError.UserOrPasswordAreInvalid -> Problem.UserOrPasswordAreInvalid.response(HttpStatus.BAD_REQUEST)
                    is UsersError.MissingField -> Problem.MissingField.response(HttpStatus.BAD_REQUEST)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to create the token")
                }
        }
    }

    @PostMapping(Uris.UsersUris.LOGOUT)
    fun logout(user: AuthenticatedUser) {
        usersService.revokeToken(user.token) // .also { removeCookies(response) }
    }

    @GetMapping(Uris.UsersUris.GET_BY_TOKEN)
    fun getUserByToken(
        @RequestHeader token: String,
    ): ResponseEntity<*> =
        when (
            val userInfo = usersService.getUserByToken(token)
        ) {
            is Success ->
                ResponseEntity.ok(
                    // todo roles = userInfo.second, is it important?
                    UserOutputPassValModel(
                        id = userInfo.value.id,
                        phoneNumber = userInfo.value.phoneNumber,
                        address = userInfo.value.address,
                        name = userInfo.value.name,
                        email = userInfo.value.email,
                        birthDate = userInfo.value.birthDate.toString(),
                        iban = userInfo.value.iban,
                        passwordValidation = userInfo.value.passwordValidation,
                        status = userInfo.value.userStatus.status,
                    ),
                )
            is Failure ->
                when (userInfo.value) {
                    is UsersError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to get the user")
                }

            else -> {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to get the user")
            }
        }

    @GetMapping(Uris.UsersUris.GET_BY_ID)
    fun getUserById(
        @PathVariable id: Int,
    ): ResponseEntity<*> =
        when (
            val userInfo = usersService.getUserById(id)
        ) {
            is Success ->
                ResponseEntity.ok(
                    UserOutputModel(
                        id = userInfo.value.first.id,
                        phoneNumber = userInfo.value.first.phoneNumber,
                        address = userInfo.value.first.address,
                        name = userInfo.value.first.name,
                        email = userInfo.value.first.email,
                        birthDate =
                            userInfo.value.first.birthDate
                                .toString(),
                        iban = userInfo.value.first.iban,
                        roles = userInfo.value.second,
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
        @RequestParam email: UsersEmailInput,
    ): ResponseEntity<*> =
        when (
            val userInfo = usersService.getUserByEmail(email.email)
        ) {
            is Success ->
                ResponseEntity.ok(
                    UserOutputModel(
                        id = userInfo.value.first.id,
                        phoneNumber = userInfo.value.first.phoneNumber,
                        address = userInfo.value.first.address,
                        name = userInfo.value.first.name,
                        email = userInfo.value.first.email,
                        birthDate =
                            userInfo.value.first.birthDate
                                .toString(),
                        iban = userInfo.value.first.iban,
                        roles = userInfo.value.second,
                    ),
                )
            is Failure ->
                when (userInfo.value) {
                    is UsersError.EmailNotFound -> Problem.EmailNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to get the user")
                }
        }

    @PostMapping("/arbnet/users/signup")
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
                        user.iban.replace(" ", ""),
                    ),
                )
        ) {
            is Success -> ResponseEntity.ok(userCreated)
            is Failure ->
                when (userCreated.value) {
                    is UsersError.NeededFullName -> Problem.NeededFullName.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidName -> Problem.InvalidName.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidPhoneNumber -> Problem.InvalidPhoneNumber.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidAddress -> Problem.InvalidAddress.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidEmail -> Problem.InvalidEmail.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidPassword -> Problem.InvalidPassword.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidBirthDate -> Problem.InvalidBirthDate.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidIban -> Problem.InvalidIban.response(HttpStatus.BAD_REQUEST)

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
                    is UsersError.InvalidName -> Problem.InvalidName.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidPhoneNumber -> Problem.InvalidPhoneNumber.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidAddress -> Problem.InvalidAddress.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidEmail -> Problem.InvalidEmail.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidPassword -> Problem.InvalidPassword.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidBirthDate -> Problem.InvalidBirthDate.response(HttpStatus.BAD_REQUEST)
                    is UsersError.InvalidIban -> Problem.InvalidIban.response(HttpStatus.BAD_REQUEST)

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

    @GetMapping(Uris.UsersUris.USER_ROLES)
    fun getAllRoles(): ResponseEntity<*> =
        when (
            val roles = usersService.getAllRoles()
        ) {
            is Success -> ResponseEntity.ok(roles)
            is Failure ->
                when (roles.value) {
                    is UsersError.RoleNotFound -> Problem.RoleNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to get the roles")
                }
        }

    @PostMapping(Uris.UsersUris.USER_CATEGORY)
    fun updateUserCategory(
        @RequestBody user: UserCategoryUpdateInputModel,
    ): ResponseEntity<*> =
        when (
            val userCategoryUpdated =
                usersService.updateUserCategory(
                    user.userId,
                    user.categoryId,
                )
        ) {
            is Success -> ResponseEntity.ok(userCategoryUpdated)
            is Failure ->
                when (userCategoryUpdated.value) {
                    is UsersError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
                    is UsersError.CategoryNotFound -> Problem.CategoryNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("failed to update the user category")
                }
        }

}
