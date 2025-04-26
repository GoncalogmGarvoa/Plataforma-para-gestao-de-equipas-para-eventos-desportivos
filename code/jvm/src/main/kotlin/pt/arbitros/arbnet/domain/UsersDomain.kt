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
}
