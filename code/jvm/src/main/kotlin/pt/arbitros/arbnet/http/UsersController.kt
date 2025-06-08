@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.arbitros.arbnet.domain.universal.Role
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
        @RequestParam roleId: RoleSelectionRequest
    ): ResponseEntity<*> {
        val userResult = usersService.getUserByToken(token)
        return when (userResult) {
            is Success -> {
                val user = userResult.value
                when (val roleResult = usersService.updateUserRoles(user.id, roleId.id, true)) {
                    is Success -> ResponseEntity.ok(roleResult)
                    is Failure -> Problem.fromApiErrorToProblemResponse(roleResult.value)
                }
            }
            is Failure -> Problem.fromApiErrorToProblemResponse(userResult.value)
        }
    }
    @GetMapping(Uris.UsersUris.USER_ROLES_FROM_USER)
    fun getAllRolesFromPlayer(
        @RequestHeader token: String,
    ): ResponseEntity<*> {
        val userResult = usersService.getUserByToken(token)

        return when (userResult) {
            is Success -> when (val result = usersService.getAllRolesFromPlayer(userResult.value.id)) {
                is Success -> ResponseEntity.ok(result)
                is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
            }
            is Failure -> Problem.fromApiErrorToProblemResponse(userResult.value)
        }

    }
    // ... existing code ...

    // ... existing code ...
//    @PostMapping(Uris.UsersUris.SET_ROLE)
//    fun setRoleUser(
//        @RequestHeader("Authorization") authorizationHeader: String,
//        @RequestParam roleId: Int
//    ): ResponseEntity<*> {
//        val token = authorizationHeader.removePrefix("Bearer ").trim()
//
//        val userResult = usersService.getUserByToken(token)
//        return when (userResult) {
//            is Success -> {
//                val user = userResult.value
//                when (val roleResult = usersService.updateUserRoles(user.id, roleId, true)) {
//                    is Success -> ResponseEntity.ok(roleResult)
//                    is Failure -> Problem.fromApiErrorToProblemResponse(roleResult.value)
//                }
//            }
//            is Failure -> Problem.fromApiErrorToProblemResponse(userResult.value)
//        }
//    }
//
//    @GetMapping(Uris.UsersUris.USER_ROLES_FROM_USER)
//    fun getAllRolesFromPlayer(
//        @RequestHeader("Authorization") authorizationHeader: String,
//    ): ResponseEntity<*> {
//        val token = authorizationHeader.removePrefix("Bearer ").trim()
//        val userResult = usersService.getUserByToken(token)
//        val aul = 0
//        return when (userResult) {
//            is Success -> when (val result = usersService.getAllRolesFromPlayer(userResult.value.id)) {
//                is Success -> ResponseEntity.ok(result)
//                is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
//            }
//            is Failure -> Problem.fromApiErrorToProblemResponse(userResult.value)
//        }
//    }
//



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

    @PostMapping("/arbnet/users/signup")
    fun createUser(
        @RequestBody user: UserInputModel,
    ): ResponseEntity<*> =
        when (
            val result =
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
            is Success -> ResponseEntity.ok(result)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    // TODO: needs to check with token if its the same user being changed
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
            is Success -> ResponseEntity.ok(userRolesUpdated)
            is Failure -> Problem.fromApiErrorToProblemResponse(userRolesUpdated.value)
        }

    @GetMapping(Uris.UsersUris.USER_ROLES)
    fun getAllRoles(): ResponseEntity<*> =
        when (
            val result = usersService.getAllRoles()
        ) {
            is Success -> ResponseEntity.ok(result)
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
            is Success -> ResponseEntity.ok(result)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

}
