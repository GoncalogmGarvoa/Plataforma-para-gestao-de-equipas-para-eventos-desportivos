package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Competition

interface CompetitionRepository {
    fun createCompetition(
        competitionName: String,
        address: String,
        phoneNumber: String,
        email: String,
        association: String,
        location: String,
    ): Int

    fun updateCompetition(
        id: Int,
        competitionName: String,
        address: String,
        phoneNumber: String,
        email: String,
        association: String,
        location: String,
    ): Boolean

    fun getCompetitionById(id: Int): Competition?


//    fun deleteCompetition(id: Int): Boolean

    fun getCompetitionIdByCallListId(callListId: Int): Int
}
