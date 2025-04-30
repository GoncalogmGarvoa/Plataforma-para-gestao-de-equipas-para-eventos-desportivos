package pt.arbitros.arbnet.repository.Aux

import pt.arbitros.arbnet.domain.Position

interface PositionRepository {
    fun createPosition(position: Position): Int

    fun findPositionById(id: Int): Position?

    fun getAllPositions(): List<Position>

    fun deletePosition(id: Int): Boolean
}
