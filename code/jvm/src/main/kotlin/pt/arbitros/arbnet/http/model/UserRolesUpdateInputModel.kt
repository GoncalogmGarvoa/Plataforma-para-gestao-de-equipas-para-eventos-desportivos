package pt.arbitros.arbnet.http.model

data class UserRolesUpdateInputModel(
    val userId: Int,
    val roles: String,
    val addOrRemove: Boolean,
)
