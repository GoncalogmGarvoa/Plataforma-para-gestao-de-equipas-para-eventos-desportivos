package pt.arbitros.arbnet.http

object Uris {
    const val PREFIX = "/arbnet"
    const val HOME = PREFIX

    object Users {
        const val GET_BY_ID = "$PREFIX/users/{id}"
        const val GET_BY_EMAIL = "$PREFIX/users/{email}"
        const val CREATE_USER = "$PREFIX/users/signup"
        const val UPDATE_USER = "$PREFIX/users/update"
    }
}
