package pt.arbitros.arbnet.repository.jdbi.adaptable_repos

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.adaptable.Position
import pt.arbitros.arbnet.repository.adaptable_repos.PositionRepository

class PositionRepositoryJdbi (
    private val handle: Handle
) : PositionRepository {

    override fun getPositionById(id: Int): Position? =
        handle
            .createQuery(
                """select * from dbp.position where id = :id""",
            ).bind("id", id)
            .mapTo<Position>()
            .singleOrNull()

    override fun getAllPositions(): List<Position> {
        return handle
            .createQuery(
                """select * from dbp.position""",
            )
            .mapTo<Position>()
            .list()
    }

    override fun getPositionIdByName(name: String): Int? =
        handle
            .createQuery(
                """select id from dbp.position where name = :name""",
            ).bind("name", name)
            .mapTo<Int>()
            .singleOrNull()

    override fun verifyPositionIds(ids: List<Int>): Boolean {
        val sql = """
        select count(*) from dbp.position
        where id in (<IDs>)
        """

        val count = handle.createQuery(sql)
            .bindList("IDs", ids)
            .mapTo(Int::class.java)
            .one()

        return count == ids.size
    }

}