package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.SessionReferee
import pt.arbitros.arbnet.http.model.SessionRefereeInputModel
import pt.arbitros.arbnet.repository.auxiliary.SessionRefereesRepository

class SessionRefereesRepositoryMem : SessionRefereesRepository {

    override fun updateSessionReferees(listSessionReferees: List<SessionReferee>): Boolean {
        TODO("Not yet implemented")
    }

}