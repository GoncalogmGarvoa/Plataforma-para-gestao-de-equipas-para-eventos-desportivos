package pt.arbitros.arbnet.domain

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