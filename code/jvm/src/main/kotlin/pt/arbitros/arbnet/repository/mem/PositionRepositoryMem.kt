package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.universal.Position
import pt.arbitros.arbnet.repository.PositionRepository

class PositionRepositoryMem: PositionRepository {
    override fun createPosition(position: Position): Int {
        TODO("Not yet implemented")
    }

    override fun getPositionById(id: Int): Position? {
        TODO("Not yet implemented")
    }

    override fun getAllPositions(): List<Position> {
        TODO("Not yet implemented")
    }

    override fun deletePosition(id: Int): Boolean {
        TODO("Not yet implemented")
    }
}
