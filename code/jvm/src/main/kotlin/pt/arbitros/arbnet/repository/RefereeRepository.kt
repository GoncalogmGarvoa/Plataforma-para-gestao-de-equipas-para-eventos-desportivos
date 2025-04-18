package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Referee

interface RefereeRepository {

    fun createReferee(referee: Referee): Boolean

    fun findRefereeById(userId: Int): Referee?

    fun deleteReferee(userId: Int): Boolean
}
