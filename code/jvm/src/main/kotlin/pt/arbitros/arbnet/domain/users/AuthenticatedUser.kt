package pt.arbitros.arbnet.domain.users

class AuthenticatedUser(
    val user: Users, // todo maybe change to something other than users
    val token: String,
)
