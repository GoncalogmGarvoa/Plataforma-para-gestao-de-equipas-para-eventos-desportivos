package pt.arbitros.arbnet.http

object Uris {
    const val PREFIX = "/arbnet"
    const val HOME = PREFIX

    object UsersUris {
        const val GET_BY_ID = "$PREFIX/users/id/{id}"
        const val GET_BY_EMAIL = "$PREFIX/users/email"
        const val CREATE_USER = "$PREFIX/users/signup"
        const val UPDATE_USER = "$PREFIX/users/update"
        const val USER_ROLES = "$PREFIX/users/roles"
        const val USER_ROLES_DELETE = "$PREFIX/users/roles/delete"
    }

    object CallListUris {
        const val CREATE_CALLLIST = "$PREFIX/callList/creation"
        const val ASSIGN_ROLES = "$PREFIX/callList/assignRoles"
        const val UPDATE_PARTICIPANT_CONFIRMATION_STATUS = "$PREFIX/callList/updateParticipant"
        const val UPDATE_CALLLISTSTAGE = "$PREFIX/callList/updateCallList"
        const val GET_CALLLIST = "$PREFIX/callList/get/{id}"
        const val UPDATE_CALLLIST = "$PREFIX/callList/update"
    }

    object ReportUris {
        const val GET_ALL_REPORTS = "$PREFIX/reports"
        const val CREATE_REPORT = "$PREFIX/reports/create"
        const val GET_REPORT_BY_ID = "$PREFIX/reports/id/{id}"
        const val UPDATE_REPORT = "$PREFIX/reports/update"
        const val SEAL_REPORT = "$PREFIX/reports/seal/{id}"
    }
}
