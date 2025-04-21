package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.CompetitionRepository

class JdbiCompetitionRepository(
    private val handle: Handle,
) : CompetitionRepository {
    override fun createCompetition(
        competitionName: String,
        competitionNumber: Int,
        address: String,
        phoneNumber: Int,
        email: String,
        association: String,
        location: String,
    ): Int {
        TODO("Not yet implemented")
    }
}
