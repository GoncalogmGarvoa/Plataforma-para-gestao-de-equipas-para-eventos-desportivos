package pt.arbitros.arbnet.http

object Uris {
    const val PREFIX = "/arbnet"
    const val HOME = PREFIX

    object UsersUris {
        const val GET_BY_ID = "$PREFIX/users/id/{id}"
        const val GET_BY_EMAIL = "$PREFIX/users/email"
        const val GET_BY_NAME = "$PREFIX/users/name"
        const val CREATE_USER = "$PREFIX/users/signup"
        const val UPDATE_USER = "$PREFIX/users/update"
        const val USER_ROLES = "$PREFIX/users/roles"
        const val USER_ROLES_DELETE = "$PREFIX/users/roles/delete"
        const val USER_CATEGORY = "$PREFIX/users/category"

        const val USERS = "$PREFIX/users" // TODO
        const val USER_ROLES_FROM_USER = "$PREFIX/users/roles/fromUser"
        const val SET_ROLE = "$PREFIX/users/role/set" // TODO
        const val TOKEN = "$PREFIX/users/token"
        const val GET_BY_TOKEN = "$PREFIX/users/me"
        const val LOGOUT = "$PREFIX/users/logout"
        const val HOME = "$PREFIX/me"
        const val GET_COOKIES = "$PREFIX/cookies" // TODO
    }

    object CallListUris {
        const val CREATE_CALLLIST = "$PREFIX/callList/creation"
        const val UPDATE_PARTICIPANT_CONFIRMATION_STATUS = "$PREFIX/callList/updateParticipant"
        const val UPDATE_CALLLISTSTAGE = "$PREFIX/callList/updateCallList"
        const val GET_CALLLIST = "$PREFIX/callList/get/{id}"
        const val UPDATE_CALLLIST = "$PREFIX/callList/update"
        const val GET_SEALED_CALLLIST = "$PREFIX/callList/sealed/{id}"
        const val GET_CALLLIST_DRAFT = "$PREFIX/callListDraft/get"

    }

    object ReportUris {
        const val GET_ALL_REPORTS = "$PREFIX/reports"
        const val CREATE_REPORT = "$PREFIX/reports/create"
        const val GET_REPORT_BY_ID = "$PREFIX/reports/id/{id}"
        const val UPDATE_REPORT = "$PREFIX/reports/update"
        const val SEAL_REPORT = "$PREFIX/reports/seal/{id}"
    }

    object EquipmentUris {
        const val GET_EQUIPMENT = "$PREFIX/equipment"
        const val SELECT_EQUIPMENT = "$PREFIX/equipment/select"
    }

    object SessionUris {
        const val FINISH_SESSION = "$PREFIX/session/finish/{id}"
        const val UPDATE_SESSION_REFEREES = "$PREFIX/session/updateReferees"
    }
}
