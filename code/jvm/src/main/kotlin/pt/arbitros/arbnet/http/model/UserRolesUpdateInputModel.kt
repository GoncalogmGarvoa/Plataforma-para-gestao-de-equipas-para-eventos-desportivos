package pt.arbitros.arbnet.http.model

data class UserRolesUpdateInputModel(
    val userId: Int,
    val roleId: Int,
    val addOrRemove: Boolean,
)
