package pt.arbitros.arbnet.domain

import org.springframework.stereotype.Component

@Component
class ParticipantDomain {

    fun validConfirmationStatus(status : String): Boolean {
        ConfirmationStatus.entries.forEach {
            if (it.value == status) {
                return true
            }
        }
        return false
    }
}