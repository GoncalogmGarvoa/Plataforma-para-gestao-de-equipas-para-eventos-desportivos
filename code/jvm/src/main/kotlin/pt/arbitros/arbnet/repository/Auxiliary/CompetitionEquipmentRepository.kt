package pt.arbitros.arbnet.repository.Aux

import pt.arbitros.arbnet.domain.Equipment

interface CompetitionEquipmentRepository {
    fun assignEquipmentToCompetition(
        competitionId: Int,
        equipmentId: Int,
    ): Boolean

    fun getEquipmentByCompetition(competitionId: Int): List<Equipment>

    fun removeEquipmentFromCompetition(
        competitionId: Int,
        equipmentId: Int,
    ): Boolean
}
