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

//    fun findCompetitionById(id: Int): Competition?
//
//    fun getAllCompetitions(): List<Competition>
//
//    fun updateCompetition(
//        id: Int,
//        name: String,
//        address: String,
//        email: String,
//        phoneNumber: String,
//        location: String,
//        association: String,
//    ): Boolean
//
//    fun deleteCompetition(id: Int): Boolean
    fun getCompetitionIdByCallListId(callListId: Int): Int
}
