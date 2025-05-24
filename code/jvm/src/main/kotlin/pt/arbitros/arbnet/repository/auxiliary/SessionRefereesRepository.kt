package pt.arbitros.arbnet.repository.auxiliary

import pt.arbitros.arbnet.domain.SessionReferee

interface SessionRefereesRepository {

    fun updateSessionReferees(
        listSessionReferees: List<SessionReferee>
    ): Boolean
}
