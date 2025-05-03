package pt.arbitros.arbnet.domain

import java.time.LocalDate
import java.time.format.DateTimeParseException

class CallListDomain {

    fun validCallListType(callListType: String): Boolean {
        CallListType.entries.forEach {
            if (it.callType == callListType) {
                return true
            }
        }
        return false
    }

    fun validDeadline(deadline: String): Boolean {
        return try {
            val date = LocalDate.parse(deadline)  // Expects ISO format
            val currentDate = LocalDate.now()
            date.isAfter(currentDate)
        } catch (e: DateTimeParseException) {
            false
        }
    }
}
