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
        const val USERS_BY_PARAMETERS = "$PREFIX/users/parameters"
        const val USERS_WITHOUT_ROLES = "$PREFIX/users/withoutRoles"
        const val GET_ALL_FUNCTIONS = "$PREFIX/users/functions"

        const val USERS = "$PREFIX/users" // TODO
        const val USER_ROLES_FROM_USER = "$PREFIX/users/roles/fromUser"
        const val SET_ROLE = "$PREFIX/users/role/set" // TODO
        const val TOKEN = "$PREFIX/users/token"
        const val GET_BY_TOKEN = "$PREFIX/users/me"
        const val LOGOUT = "$PREFIX/users/logout"
        const val HOME = "$PREFIX/me"
        const val GET_COOKIES = "$PREFIX/cookies" // TODO
        const val NOTIFICATIONS = "$PREFIX/users/notifications"
        const val NOTIFICATIONS_READ = "$PREFIX/users/notifications/read/{notificationId}"
        const val USER_STATUS = "$PREFIX/users/status"
    }

    object CallListUris {
        const val CREATE_CALLLIST = "$PREFIX/callList/creation"
        const val UPDATE_PARTICIPANT_CONFIRMATION_STATUS = "$PREFIX/callList/updateParticipant"
        const val UPDATE_CALLLISTSTAGE = "$PREFIX/callList/updateCallListStage"
        const val GET_CALLLIST = "$PREFIX/callList/get/{id}"
        const val UPDATE_CALLLIST = "$PREFIX/callList/update"
        const val GET_SEALED_CALLLIST = "$PREFIX/callList/sealed/{id}"
        const val GET_CALLLIST_DRAFT = "$PREFIX/callListDraft/get"
        const val GET_CALLLISTS_WITH_REFEREE = "$PREFIX/callList/referee/{refereeId}"
        const val CANCEL_CALLLIST = "$PREFIX/callList/cancel"

    }

    object ReportUris {
        const val GET_ALL_REPORTS = "$PREFIX/reports"
        const val CREATE_REPORT = "$PREFIX/reports/create"
        const val GET_REPORT_BY_ID = "$PREFIX/reports/id/{id}"
        const val UPDATE_REPORT = "$PREFIX/reports/update"
        const val SEAL_REPORT = "$PREFIX/reports/seal/{id}"
        const val GET_ALL_REPORTS_BY_COMPETITION = "$PREFIX/reports/competition/{competitionId}"
    }

    object PaymentsUris {
        const val GET_ALL_PAYMENT_REPORTS = "$PREFIX/payments"
        const val CREATE_PAYMENT_REPORT = "$PREFIX/payments/create"
        const val GET_PAYMENT_REPORTS_BY_COMPETITION = "$PREFIX/payments/competition/{competitionId}"
        const val GET_PAYMENT_REPORT_BY_ID = "$PREFIX/payments/id/{id}"
        const val UPDATE_PAYMENT_REPORT = "$PREFIX/payments/update"
        const val SEAL_PAYMENT_REPORT = "$PREFIX/payments/seal/{id}"
    }

    object SessionUris {
        const val FINISH_SESSION = "$PREFIX/session/finish/{id}"
        const val UPDATE_SESSION_REFEREES = "$PREFIX/session/updateReferees"
    }

    object EquipmentUris {
        const val GET_ALL_EQUIPMENT = "$PREFIX/equipment"
    }
}
