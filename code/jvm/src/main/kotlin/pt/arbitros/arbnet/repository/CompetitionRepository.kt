package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Competition

interface CompetitionRepository {

    fun createCompetition(competition: Competition): Int

    fun findCompetitionById(id: Int): Competition?

    fun getAllCompetitions(): List<Competition>

    fun updateCompetition(competition: Competition): Boolean

    fun deleteCompetition(id: Int): Boolean
}
