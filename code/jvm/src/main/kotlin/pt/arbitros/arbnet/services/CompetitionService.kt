package pt.arbitros.arbnet.services

import pt.arbitros.arbnet.domain.Competition
import pt.arbitros.arbnet.domain.CompetitionDomain
import pt.arbitros.arbnet.repository.TransactionManager

class CompetitionService(
    private val transactionManager: TransactionManager,
    private val competitionDomain: CompetitionDomain,
    // private val clock: Clock
) {
    fun createCompetition(
        name: String,
        address: String,
        email: String,
        phoneNumber: String,
        location: String,
        association: String,
    ): Int =
        transactionManager.run {
            val competitionRepository = it.competitionRepository
            val returnedId =
                competitionRepository.createCompetition(
                    name,
                    address,
                    email,
                    phoneNumber,
                    location,
                    association,
                )
            returnedId
        }

    fun getCompetitionById(id: Int): Competition =
        transactionManager.run {
            val competitionRepository = it.competitionRepository
            val competition =
                competitionRepository.findCompetitionById(id)
                    ?: throw Exception("Competition with id $id not found")
            competition
        }

    fun getAllCompetitions(): List<Competition> =
        transactionManager.run {
            val competitionRepository = it.competitionRepository
            val competitions = competitionRepository.getAllCompetitions()
            competitions
        }

    fun updateCompetition(
        id: Int,
        name: String,
        address: String,
        email: String,
        phoneNumber: String,
        location: String,
        association: String,
    ): Boolean =
        transactionManager.run {
            val competitionRepository = it.competitionRepository
            val updated =
                competitionRepository.updateCompetition(
                    id,
                    name,
                    address,
                    email,
                    phoneNumber,
                    location,
                    association,
                )
            updated
        }

    fun deleteCompetition(id: Int): Boolean =
        transactionManager.run {
            val competitionRepository = it.competitionRepository
            val deleted = competitionRepository.deleteCompetition(id)
            deleted
        }
}
