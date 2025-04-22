package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.*

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val usersRepository: UsersRepository = JdbiUsersRepository(handle)
    override val callListRepository: CallListRepository = JdbiCallListRepository(handle)
    override val competitionRepository: CompetitionRepository = JdbiCompetitionRepository(handle)
    override val sessionsRepository: SessionsRepository = JdbiSessionsRepository(handle)
    override val matchDayRepository: MatchDayRepository = JdbiMatchDayRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}
