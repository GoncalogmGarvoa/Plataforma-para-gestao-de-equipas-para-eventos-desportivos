package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.repository.adaptable_repos.*
import pt.arbitros.arbnet.repository.auxiliary.SessionRefereesRepository

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
    val reportRepository: ReportRepository
    val equipmentRepository: EquipmentRepository
    val categoryRepository: CategoryRepository
    val categoryDirRepository: CategoryDirRepository
    val sessionRefereesRepository: SessionRefereesRepository
    val positionRepository: PositionRepository
    val paymentValuesRepository: PaymentValuesRepository

    fun rollback()
}
