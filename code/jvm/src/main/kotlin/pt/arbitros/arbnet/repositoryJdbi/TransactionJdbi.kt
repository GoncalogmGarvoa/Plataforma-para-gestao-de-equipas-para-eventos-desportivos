@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.repositoryJdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.*

class TransactionJdbi(
    private val handle: Handle,
) : Transaction {
    override val usersRepository: UsersRepository = UsersRepositoryJdbi(handle)
    override val callListRepository: CallListRepository = CallListRepositoryJdbi(handle)
    override val competitionRepository: CompetitionRepository = CompetitionRepositoryJdbi(handle)
    override val sessionsRepository: SessionsRepository = SessionsRepositoryJdbi(handle)
    override val matchDayRepository: MatchDayRepository = MatchDayRepositoryJdbi(handle)
    override val participantRepository: ParticipantRepository = ParticipantRepositoryJdbi(handle)
    override val roleRepository: RoleRepository = RepositoryRoleJdbi(handle)

    override fun rollback() {
        handle.rollback()
    }
}
