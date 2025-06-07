package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.universal.Equipment
import pt.arbitros.arbnet.repository.EquipmentRepository

class EquipmentRepositoryMem: EquipmentRepository {
    override fun getEquipment(): List<Equipment> {
        TODO("Not yet implemented")
    }

    override fun selectEquipment(
        competitionId: Int,
        equipmentsId: List<Int>
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun verifyEquipmentId(equipmentsId: List<Int>): Boolean {
        TODO("Not yet implemented")
    }

    override fun getEquipmentByCompetitionId(competitionId: Int): List<Equipment> {
        TODO("Not yet implemented")
    }

    override fun deleteEquipmentByCompetitionId(competitionId: Int): Boolean {
        TODO("Not yet implemented")
    }
}