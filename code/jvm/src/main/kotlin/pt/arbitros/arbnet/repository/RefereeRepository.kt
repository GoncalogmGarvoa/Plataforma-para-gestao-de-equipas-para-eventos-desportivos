package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Referee

interface RefereeRepository {
    fun createReferee(userId: Int): Boolean

    //fun findRefereeById(userId: Int): Referee?

    fun getAllReferees(referees: List<Int>): List<Referee>

    fun deleteReferee(userId: Int): Boolean
}