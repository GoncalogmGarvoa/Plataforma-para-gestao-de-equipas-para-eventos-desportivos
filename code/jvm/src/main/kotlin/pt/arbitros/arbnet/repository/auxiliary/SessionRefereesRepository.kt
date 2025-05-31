package pt.arbitros.arbnet.repository.auxiliary

import pt.arbitros.arbnet.domain.SessionReferee

//TODO is this supposed to be in the auxiliary package?
interface SessionRefereesRepository {

    fun updateSessionReferees(
        listSessionReferees: List<SessionReferee>
    ): Boolean
}
