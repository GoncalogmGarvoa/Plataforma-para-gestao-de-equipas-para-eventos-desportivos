package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.ArbitrationCouncil
import pt.arbitros.arbnet.repository.ArbitrationCouncilRepository

class ArbitrationCouncilRepositoryJdbi(
    private val handle: Handle
) : ArbitrationCouncilRepository {

    override fun createCouncilMember(userId: Int): Boolean =
        handle.createUpdate(
                """INSERT INTO arbitration_council (user_id) VALUES (:userId)"""
            )
            .bind("userId", userId)
            .execute() > 0

    override fun getCouncilMemberById(userId: Int) =
        handle.createUpdate("""
                        SELECT * FROM arbitration_council WHERE user_id = :userId
                    """)
            .bind("userId", userId)
            .executeAndReturnGeneratedKeys()
            .mapTo<ArbitrationCouncil>()
            .singleOrNull()

    override fun deleteCouncilMember(userId: Int): Boolean =
        handle.createUpdate(
                """DELETE FROM arbitration_council WHERE user_id = :userId"""
            )
            .bind("userId", userId)
            .execute() > 0
}






