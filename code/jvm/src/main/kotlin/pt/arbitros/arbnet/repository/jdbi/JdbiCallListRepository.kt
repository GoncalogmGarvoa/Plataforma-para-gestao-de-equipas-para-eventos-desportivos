package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.repository.CallListRepository
import java.time.LocalDate

class JdbiCallListRepository(
    private val handle: Handle,
) : CallListRepository {
    override fun createCallList(
        competitionName: String,
        competitionNumber: Int,
        address: String,
        phoneNumber: Int,
        email: String,
        association: String,
        location: String,
        participant: List<String>,
        timeLine: LocalDate,
        type: String,
    ): Int {
        TODO("Not yet implemented")
    }
}
