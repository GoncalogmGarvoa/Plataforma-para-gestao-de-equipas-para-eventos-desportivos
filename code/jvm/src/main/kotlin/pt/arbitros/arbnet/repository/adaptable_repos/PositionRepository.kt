package pt.arbitros.arbnet.repository.adaptable_repos

import pt.arbitros.arbnet.domain.universal.Position

interface PositionRepository {

    fun getPositionById(id: Int): Position?

    fun getAllPositions(): List<Position>

    fun getPositionNameById(id: Int): String?

    fun verifyPositionIds(ids: List<Int>): Boolean

}