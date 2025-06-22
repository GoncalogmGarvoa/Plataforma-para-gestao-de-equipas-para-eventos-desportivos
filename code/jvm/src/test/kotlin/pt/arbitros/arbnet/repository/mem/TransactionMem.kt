@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.repository.mem

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.*
import pt.arbitros.arbnet.repository.auxiliary.SessionRefereesRepository
import pt.arbitros.arbnet.repository.adaptable_repos.CategoryRepository
import pt.arbitros.arbnet.repository.adaptable_repos.EquipmentRepository
import pt.arbitros.arbnet.repository.adaptable_repos.FunctionRepository
import pt.arbitros.arbnet.repository.adaptable_repos.PositionRepository
import pt.arbitros.arbnet.repository.adaptable_repos.RoleRepository

class TransactionMem(
    private val handle: Handle,

    ) : Transaction {

    override val usersRepository: UsersRepository = UsersRepositoryMem()
    override val callListRepository: CallListRepository = CallListRepositoryMem()
    override val competitionRepository: CompetitionRepository = CompetitionRepositoryMem()
    override val sessionsRepository: SessionsRepository = SessionsRepositoryMem()
    override val matchDayRepository: MatchDayRepository = MatchDayRepositoryMem()
    override val participantRepository: ParticipantRepository = ParticipantRepositoryMem()
    override val functionRepository: FunctionRepository = FunctionRepositoryMem()
    override val usersRolesRepository: UsersRolesRepository = UsersRolesRepositoryMem()
    override val roleRepository: RoleRepository = RoleRepositoryMem()
    override val reportRepository : ReportRepository = ReportRepositoryMem()
    override val equipmentRepository: EquipmentRepository = EquipmentRepositoryMem()
    override val categoryRepository: CategoryRepository = CategoryRepositoryMem()
    override val categoryDirRepository: CategoryDirRepository = CategoryDirRepositoryMem()
    override val sessionRefereesRepository: SessionRefereesRepository = SessionRefereesRepositoryMem()
    override val positionRepository: PositionRepository = PositionRepositoryMem()

    override fun rollback() {
        handle.rollback()
    }
}
