@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.repository.mem

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.*

class TransactionMem(
    private val handle: Handle,

) : Transaction {

    override val arbitrationCouncilRepository: ArbitrationCouncilRepository = ArbitrationCouncilRepositoryMem()
    override val refereeRepository: RefereeRepository = RefereeRepositoryMem()

    override val adminRepository: AdminRepository = AdminRepositoryMem()
    override val usersRepository: UsersRepository = UsersRepositoryMem()
    override val callListRepository: CallListRepository = CallListRepositoryMem()
    override val competitionRepository: CompetitionRepository = CompetitionRepositoryMem()
    override val sessionsRepository: SessionsRepository = SessionsRepositoryMem()
    override val matchDayRepository: MatchDayRepository = MatchDayRepositoryMem()
    override val participantRepository: ParticipantRepository = ParticipantRepositoryMem()
    override val functionRepository: FunctionRepository = FunctionRepositoryMem()

    override fun rollback() {
        handle.rollback()
    }
}
