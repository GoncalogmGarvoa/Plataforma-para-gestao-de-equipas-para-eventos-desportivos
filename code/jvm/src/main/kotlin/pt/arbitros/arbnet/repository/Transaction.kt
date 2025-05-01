package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.repository.AdminRepository
import pt.arbitros.arbnet.repository.Aux.ArbitrationCouncilRepository
import pt.arbitros.arbnet.repository.RefereeRepository

interface Transaction {
    val usersRepository: UsersRepository

    val competitionRepository: CompetitionRepository
    val callListRepository: CallListRepository
    val matchDayRepository: MatchDayRepository
    val sessionsRepository: SessionsRepository
    val participantRepository: ParticipantRepository
    val roleRepository: RoleRepository
    val arbitrationCouncilRepository: ArbitrationCouncilRepository
    val adminRepository: AdminRepository
    val refereeRepository: RefereeRepository

    fun rollback()
}
