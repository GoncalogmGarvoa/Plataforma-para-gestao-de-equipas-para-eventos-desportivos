package pt.arbitros.arbnet.repository.adaptable_repos

import pt.arbitros.arbnet.domain.adaptable.Position

interface PositionRepository {

    fun getPositionById(id: Int): Position?

    fun getAllPositions(): List<Position>

    fun getPositionIdByName(name: String): Int?

    fun verifyPositionIds(ids: List<Int>): Boolean

}