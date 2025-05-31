package pt.arbitros.arbnet.domain.users

class AuthenticatedUser(
    val user: User, // todo maybe change to something other than users
    val token: String,
)
