@file:Suppress("ktlint:standard:filename")

package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.Competition
import pt.arbitros.arbnet.repository.CompetitionRepository

class CompetitionRepositoryMem : CompetitionRepository {
    private val competitions = mutableListOf<Competition>()
    private var nextId = 1

    override fun createCompetition(
        competitionName: String,
        address: String,
        phoneNumber: String,
        email: String,
        association: String,
        location: String,
    ): Int {
        val competition =
            Competition(
                competitionNumber = nextId++,
                name = competitionName,
                address = address,
                phoneNumber = phoneNumber.toString(),
                email = email,
                association = association,
                location = location,
            )
        competitions.add(competition)
        return competition.competitionNumber
    }

    override fun updateCompetition(
        id: Int,
        competitionName: String,
        address: String,
        phoneNumber: String,
        email: String,
        association: String,
        location: String
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCompetitionById(id: Int): Competition? {
        TODO("Not yet implemented")
    }

    override fun getCompetitionIdByCallListId(callListId: Int): Int {
        TODO("Not yet implemented")
    }
}
