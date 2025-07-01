package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.adaptable.Position
import pt.arbitros.arbnet.repository.adaptable_repos.PositionRepository

class PositionRepositoryMem: PositionRepository {

    override fun getPositionById(id: Int): Position? {
        TODO("Not yet implemented")
    }

    override fun getAllPositions(): List<Position> {
        TODO("Not yet implemented")
    }

    override fun getPositionIdByName(name: String): Int? {
        TODO("Not yet implemented")
    }

    override fun verifyPositionIds(ids: List<Int>): Boolean {
        TODO("Not yet implemented")
    }

    override fun verifyPositionNames(names: List<String>): Boolean {
        TODO("Not yet implemented")
    }

}
