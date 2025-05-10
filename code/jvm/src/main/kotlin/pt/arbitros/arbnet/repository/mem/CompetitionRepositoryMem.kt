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
}
