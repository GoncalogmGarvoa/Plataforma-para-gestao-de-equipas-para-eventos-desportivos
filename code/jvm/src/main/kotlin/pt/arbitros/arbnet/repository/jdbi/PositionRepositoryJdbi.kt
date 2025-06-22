package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.universal.Position
import pt.arbitros.arbnet.repository.adaptable_repos.PositionRepository

class PositionRepositoryJdbi (
    private val handle: Handle
) : PositionRepository {
    override fun createPosition(position: Position): Int {
        TODO("Not yet implemented")
    }

    override fun getPositionById(id: Int): Position? =
        handle
            .createQuery(
                """select * from dbp.position where id = :id""",
            ).bind("id", id)
            .mapTo<Position>()
            .singleOrNull()

    override fun getAllPositions(): List<Position> {
        TODO("Not yet implemented")
    }

    override fun deletePosition(id: Int): Boolean {
        TODO("Not yet implemented")
    }
}