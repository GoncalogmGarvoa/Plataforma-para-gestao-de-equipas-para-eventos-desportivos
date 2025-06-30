package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.adaptable.Equipment

class EquipmentRepositoryMem: EquipmentRepository {
    override fun getAllEquipment(): List<Equipment> {
        TODO("Not yet implemented")
    }

    override fun selectEquipment(
        competitionId: Int,
        equipmentsId: List<Int>
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun verifyEquipmentIds(equipmentsId: List<Int>): Boolean {
        TODO("Not yet implemented")
    }

    override fun getEquipmentByCompetitionId(competitionId: Int): List<Equipment> {
        TODO("Not yet implemented")
    }

    override fun deleteEquipmentByCompetitionId(competitionId: Int): Boolean {
        TODO("Not yet implemented")
    }
}