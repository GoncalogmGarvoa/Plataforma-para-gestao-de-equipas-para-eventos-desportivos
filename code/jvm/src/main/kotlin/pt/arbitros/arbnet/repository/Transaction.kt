package pt.arbitros.arbnet.repository

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
