@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.http

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.arbitros.arbnet.domain.users.AuthenticatedUser
import pt.arbitros.arbnet.http.model.UserStatusInput
import pt.arbitros.arbnet.http.model.users.UserCategoryUpdateInputModel
import pt.arbitros.arbnet.http.model.users.UserCreateTokenInputModel
import pt.arbitros.arbnet.http.model.users.UserCreationInputModel
import pt.arbitros.arbnet.http.model.users.UserNameId
import pt.arbitros.arbnet.http.model.users.UserOutputModel
import pt.arbitros.arbnet.http.model.users.UserOutputPassValModel
import pt.arbitros.arbnet.http.model.users.UserRolesUpdateInputModel
import pt.arbitros.arbnet.http.model.users.UserTokenCreateOutputModel
import pt.arbitros.arbnet.http.model.users.UserUpdateInputModel
import pt.arbitros.arbnet.http.model.users.UsersEmailInput
import pt.arbitros.arbnet.services.*

@RestController
class UsersController(
    private val usersService: UsersService,
) {

    @PostMapping(Uris.UsersUris.TOKEN) // equal to logging in
    fun login(
        @RequestBody input: UserCreateTokenInputModel,
    ): ResponseEntity<*> {

        val aux = usersService.checkPassowrd(input.password)
        val res = usersService.createToken(input.email, input.password)
        return when (res) {
            is Success -> ResponseEntity.ok(UserTokenCreateOutputModel(res.value.tokenValue))
            is Failure -> Problem.fromApiErrorToProblemResponse(res.value)
        }
    }

    @PostMapping(Uris.UsersUris.LOGOUT)
    fun logout(user: AuthenticatedUser) {
        usersService.revokeToken(user.token) // .also { removeCookies(response) }
    }

    data class RoleSelectionRequest(val id: Int)
    @PostMapping(Uris.UsersUris.SET_ROLE)
    fun setRoleUser(
        @RequestHeader token: String,
        @RequestBody roleSelectionRequest: RoleSelectionRequest
    ): ResponseEntity<*> {
        val userResult = usersService.getUserByToken(token)
        return when (userResult) {
            is Success -> {
                val user = userResult.value
                when (val roleResult = usersService.setUserTokenRole(user.id,token,roleSelectionRequest.id)) {
                    is Success -> ResponseEntity.ok(roleResult)
                    is Failure -> Problem.fromApiErrorToProblemResponse(roleResult.value)
                }
            }
            is Failure -> Problem.fromApiErrorToProblemResponse(userResult.value)
        }
    }


    @GetMapping(Uris.UsersUris.USER_ROLES_FROM_USER)
    fun getAllRolesFromUser(
        @RequestHeader token: String,
    ): ResponseEntity<*> {
        return when (val userResult = usersService.getUserByToken(token)) {
            is Success -> when (val result = usersService.getAllRolesFromUser(userResult.value.id)) {
                is Success -> ResponseEntity.ok(result)
                is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
            }
            is Failure -> Problem.fromApiErrorToProblemResponse(userResult.value)
        }

    }



    @GetMapping(Uris.UsersUris.GET_BY_TOKEN)
    fun getUserByToken(
        @RequestHeader token: String,
    ): ResponseEntity<*> =
        when (
            val result = usersService.getUserByToken(token)
        ) {
            is Success ->
                ResponseEntity.ok(
                    // todo roles = userInfo.second, is it important?
                    UserOutputPassValModel(
                        id = result.value.id,
                        phoneNumber = result.value.phoneNumber,
                        address = result.value.address,
                        name = result.value.name,
                        email = result.value.email,
                        birthDate = result.value.birthDate.toString(),
                        iban = result.value.iban,
                        passwordValidation = result.value.passwordValidation,
                        status = result.value.userStatus.status,
                    ),
                )
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.UsersUris.GET_BY_ID)
    fun getUserById(
        @PathVariable id: Int,
    ): ResponseEntity<*> =
        when (
            val result = usersService.getUserById(id)
        ) {
            is Success ->
                ResponseEntity.ok(
                    UserOutputModel(
                        id = result.value.first.id,
                        phoneNumber = result.value.first.phoneNumber,
                        address = result.value.first.address,
                        name = result.value.first.name,
                        email = result.value.first.email,
                        birthDate =
                            result.value.first.birthDate
                                .toString(),
                        iban = result.value.first.iban,
                        roles = result.value.second,
                    ),
                )
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.UsersUris.GET_BY_NAME)
    fun getUsersByName(
        @RequestParam name: String,
    ): ResponseEntity<*> =
        when (
            val result = usersService.getUsersByName(name)
        ) {
            is Success ->
                ResponseEntity.ok(
                    result.value.map {
                        UserNameId(it.name, it.id)
                    },
                )
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }
    @GetMapping(Uris.UsersUris.GET_ALL_USERS)
    fun getAllUsers(): ResponseEntity<*> =
        when (
            val result = usersService.getAllUsers()
        ) {
            is Success ->
                ResponseEntity.ok(
                    result.value.map {
                        UserOutputModel(
                            id = it.id,
                            phoneNumber = it.phoneNumber,
                            address = it.address,
                            name = it.name,
                            email = it.email,
                            birthDate = it.birthDate.toString(),
                            iban = it.iban,
                            roles = it.roles,
                        )
                    },
                )
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }


    @GetMapping(Uris.UsersUris.GET_BY_EMAIL)
    fun getUserByEmail(
        @RequestParam email: UsersEmailInput,
    ): ResponseEntity<*> =
        when (
            val result = usersService.getUserByEmail(email.email)
        ) {
            is Success ->
                ResponseEntity.ok(
                    UserOutputModel(
                        id = result.value.first.id,
                        phoneNumber = result.value.first.phoneNumber,
                        address = result.value.first.address,
                        name = result.value.first.name,
                        email = result.value.first.email,
                        birthDate =
                            result.value.first.birthDate
                                .toString(),
                        iban = result.value.first.iban,
                        roles = result.value.second,
                    ),
                )
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @PostMapping(Uris.UsersUris.CREATE_USER)
    fun createUser(
        @RequestBody user: UserCreationInputModel,
    ): ResponseEntity<*> =
        when (
            val result =
                usersService.createUser(
                    UserCreationInputModel(
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
            is Success -> ResponseEntity.ok(result)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }


    @PutMapping(Uris.UsersUris.UPDATE_USER)
    fun updateUser(
        @RequestBody user: UserUpdateInputModel,
    ): ResponseEntity<*> =
        when (
            val result =
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
            is Success -> ResponseEntity.ok(result)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
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
            is Success -> ResponseEntity.ok(userRolesUpdated.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(userRolesUpdated.value)
        }

    @GetMapping(Uris.UsersUris.USER_ROLES)
    fun getAllRoles(): ResponseEntity<*> =
        when (
            val result = usersService.getAllRoles()
        ) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.UsersUris.CATEGORIES)
    fun getAllCategories(): ResponseEntity<*> =
        when (
            val result = usersService.getAllCategories()
        ) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.UsersUris.USER_CATEGORY)
    fun getUserCategory(
        @RequestParam userId: Int,
    ): ResponseEntity<*> =
        when (
            val result = usersService.getUserCategory(userId)
        ) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @PostMapping(Uris.UsersUris.USER_CATEGORY)
    fun updateUserCategory(
        @RequestBody user: UserCategoryUpdateInputModel,
    ): ResponseEntity<*> =
        when (
            val result =
                usersService.updateUserCategory(
                    user.userId,
                    user.categoryId,
                )
        ) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.UsersUris.USERS_BY_PARAMETERS)
    fun getUsersByParameters(
        @RequestParam userName: String,
        @RequestParam userRoles: List<String>
    ): ResponseEntity<*> =
        when (
            val result = usersService.getUsersByParameters(userName, userRoles)
        ) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.UsersUris.USERS_WITHOUT_ROLES)
    fun getUsersWithoutRoles(
        @RequestParam userName: String,
    ) : ResponseEntity<*> =
        when (
            val result = usersService.getUsersWithoutRoles(userName)
        ) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.UsersUris.NOTIFICATIONS)
    fun getNotificationsByUser(
        @RequestHeader token: String,

        ): ResponseEntity<*> {
        val userResult = usersService.getUserByToken(token)
        return if (userResult is Success) {
            when (
            val result = usersService.getNotificationsByUserAndRoleIds(userResult.value.id)
        ) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

        }
        else {
            Problem.fromApiErrorToProblemResponse(
                ApiError.NotFound(
                    "User not found or not authorized to create a call list",
                ))
        }
    }

    @PutMapping(Uris.UsersUris.NOTIFICATIONS_READ)
    fun changeNotificationStatus(
        @PathVariable notificationId: Int,
    ): ResponseEntity<*> {

        return when (
            val result = usersService.changeNotificationStatus(notificationId)
        ) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }
    }

    @GetMapping(Uris.UsersUris.GET_ALL_FUNCTIONS)
    fun getAllFunctions(): ResponseEntity<*> =
        when (val result = usersService.getAllFunctions()) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }


    @PutMapping(Uris.UsersUris.USER_STATUS)
    fun changeUserStatus(
        @RequestBody userStatusInput: UserStatusInput
    ): ResponseEntity<*> =
        when (val result = usersService.changeUserStatus(userStatusInput)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }


    @GetMapping(Uris.UsersUris.INACTIVE_USERS)
    fun getInactiveUsers(): ResponseEntity<*> =
        when (val result = usersService.getInactiveUsers()) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @PostMapping(Uris.UsersUris.INVITE_NEW_USER)
    fun inviteNewUser(
        @RequestBody email: String,  //todo create object for this
    ): ResponseEntity<*> =
        when (val result = usersService.sendInvite(email.trim())) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

}
