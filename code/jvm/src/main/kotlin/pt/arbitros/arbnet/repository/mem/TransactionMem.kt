@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.repository.mem

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.*
import pt.arbitros.arbnet.repository.jdbi.EquipmentRepositoryJdbi
import pt.arbitros.arbnet.repository.jdbi.ReportRepositoryJdbi

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
    override val reportRepository : ReportRepository = ReportRepositoryJdbi(handle) //todo change to mem
    override val equipmentRepository: EquipmentRepository = EquipmentRepositoryJdbi(handle)


    override fun rollback() {
        handle.rollback()
    }
}
