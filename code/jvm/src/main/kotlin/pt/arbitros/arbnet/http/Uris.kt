package pt.arbitros.arbnet.http

object Uris {
    const val PREFIX = "/arbnet"
    const val HOME = PREFIX

    object UsersUris {
        const val GET_BY_ID = "$PREFIX/users/id/{id}"
        const val GET_BY_EMAIL = "$PREFIX/users/email"
        const val CREATE_USER = "$PREFIX/users/signup"
        const val UPDATE_USER = "$PREFIX/users/update"
    }

    object CallListUris {
        const val CREATE_CALLLIST = "$PREFIX/callList/creation"
    }
}
