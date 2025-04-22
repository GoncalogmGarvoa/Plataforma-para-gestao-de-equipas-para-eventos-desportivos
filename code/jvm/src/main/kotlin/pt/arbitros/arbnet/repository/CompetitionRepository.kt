package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Competition

interface CompetitionRepository {
    fun createCompetition(
        name: String,
        address: String,
        email: String,
        phoneNumber: String,
        location: String,
        association: String,
    ): Int

    fun findCompetitionById(id: Int): Competition?

    fun getAllCompetitions(): List<Competition>

    fun updateCompetition(
        id: Int,
        name: String,
        address: String,
        email: String,
        phoneNumber: String,
        location: String,
        association: String,
    ): Boolean

    fun deleteCompetition(id: Int): Boolean
}
