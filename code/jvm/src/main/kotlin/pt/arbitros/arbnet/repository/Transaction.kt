package pt.arbitros.arbnet.repository

interface Transaction {
    val usersRolesRepository: UsersRolesRepository
    val usersRepository: UsersRepository

    val competitionRepository: CompetitionRepository
    val callListRepository: CallListRepository
    val matchDayRepository: MatchDayRepository
    val sessionsRepository: SessionsRepository
    val participantRepository: ParticipantRepository
    val functionRepository: FunctionRepository
    val roleRepository: RoleRepository
    val reportRepository: ReportSQLRepository

    fun rollback()
}
