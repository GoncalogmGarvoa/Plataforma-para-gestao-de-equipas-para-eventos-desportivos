package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.universal.Position

interface PositionRepository {
    fun createPosition(position: Position): Int

    fun getPositionById(id: Int): Position?

    fun getAllPositions(): List<Position>

    fun deletePosition(id: Int): Boolean
}