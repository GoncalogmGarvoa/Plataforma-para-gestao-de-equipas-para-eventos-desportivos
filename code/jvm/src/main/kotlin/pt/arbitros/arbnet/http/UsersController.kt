package pt.arbitros.arbnet.http

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.http.model.UserOutputModel
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.UsersService

typealias userEither = Either<Int, Users>

@RestController
class UsersController(
    private val usersService: UsersService
) {

    @GetMapping(Uris.Users.GET_BY_ID)
    fun getUserById(
        @PathVariable id: Int
    ): UserOutputModel? {
        val aux = usersService.getUserById(id)
        return UserOutputModel(
            id = aux.id,
            roles = aux.roles
        )
    }


}
