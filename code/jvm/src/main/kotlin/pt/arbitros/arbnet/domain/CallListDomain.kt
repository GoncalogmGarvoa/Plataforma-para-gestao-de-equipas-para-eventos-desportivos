package pt.arbitros.arbnet.domain

import org.springframework.stereotype.Component

@Component
class CallListDomain {
    fun validCallListType(callListType: String): Boolean = callListType in CallListType.entries.map { it.callType }
}
