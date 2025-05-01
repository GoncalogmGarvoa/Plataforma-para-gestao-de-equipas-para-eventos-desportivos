package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.repository.Aux.ArbitrationCouncilRepository

interface Transaction {
    val usersRepository: UsersRepository

    val competitionRepository: CompetitionRepository
    val callListRepository: CallListRepository
    val matchDayRepository: MatchDayRepository
    val sessionsRepository: SessionsRepository
    val participantRepository: ParticipantRepository
    val roleRepository: RoleRepository
    val arbitrationCouncilRepository: ArbitrationCouncilRepository

    fun rollback()
}
