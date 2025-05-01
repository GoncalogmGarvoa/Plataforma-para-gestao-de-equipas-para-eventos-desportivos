package pt.arbitros.arbnet.domain

import org.springframework.stereotype.Component

@Component
class UsersDomain {
    fun validRole(roles: String): Boolean {
        UserRole.entries.forEach {
            if (it.roleName == roles) {
                return true
            }
        }
        return false
    }

    fun validStatus(status: String): Boolean {
        UserStatus.entries.forEach {
            if (it.status == status) {
                return true
            }
        }
        return false
    }

    fun positiveNumberUserId(userId: Int): Boolean {
        return userId > 0
    }

    //TODO
    fun validPhoneNumber(phoneNumber: Int): Boolean {
        val phoneNumberString = phoneNumber.toString()
        return phoneNumberString.length == 9
    }
}
