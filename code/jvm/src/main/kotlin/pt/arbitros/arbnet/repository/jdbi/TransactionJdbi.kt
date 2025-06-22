@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.*
import pt.arbitros.arbnet.repository.CategoryDirRepository
import pt.arbitros.arbnet.repository.adaptable_repos.CategoryRepository
import pt.arbitros.arbnet.repository.auxiliary.SessionRefereesRepository
import pt.arbitros.arbnet.repository.adaptable_repos.EquipmentRepository
import pt.arbitros.arbnet.repository.adaptable_repos.FunctionRepository
import pt.arbitros.arbnet.repository.adaptable_repos.PositionRepository
import pt.arbitros.arbnet.repository.adaptable_repos.RoleRepository

class TransactionJdbi(
    private val handle: Handle,

) : Transaction {
    override val usersRepository: UsersRepository = UsersRepositoryJdbi(handle)
    override val callListRepository: CallListRepository = CallListRepositoryJdbi(handle)
    override val competitionRepository: CompetitionRepository = CompetitionRepositoryJdbi(handle)
    override val sessionsRepository: SessionsRepository = SessionsRepositoryJdbi(handle)
    override val matchDayRepository: MatchDayRepository = MatchDayRepositoryJdbi(handle)
    override val participantRepository: ParticipantRepository = ParticipantRepositoryJdbi(handle)
    override val functionRepository: FunctionRepository = FunctionRepositoryJdbi(handle)
    override val usersRolesRepository: UsersRolesRepository = UsersRolesRepositoryJdbi(handle)
    override val roleRepository: RoleRepository = RoleRepositoryJdbi(handle)
    override val reportRepository: ReportRepository = ReportRepositoryJdbi(handle)
    override val equipmentRepository: EquipmentRepository = EquipmentRepositoryJdbi(handle)
    override val categoryRepository: CategoryRepository = CategoryRepositoryJdbi(handle)
    override val categoryDirRepository: CategoryDirRepository = CategoryDirRepositoryJdbi(handle)
    override val sessionRefereesRepository: SessionRefereesRepository = SessionRefereesRepositoryJdbi(handle)
    override val positionRepository: PositionRepository = PositionRepositoryJdbi(handle)

    override fun rollback() {
        handle.rollback()
    }
}
