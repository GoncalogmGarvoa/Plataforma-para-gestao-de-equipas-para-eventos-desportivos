package pt.arbitros.arbnet.domain.users

import pt.arbitros.arbnet.domain.adaptable.Role

class AuthenticatedUser(
    val user: User,
    val token: String,
    val role : Role = Role(0, "user")
)
