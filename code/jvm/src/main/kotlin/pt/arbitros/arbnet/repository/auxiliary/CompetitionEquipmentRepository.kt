package pt.arbitros.arbnet.repository.auxiliary

import pt.arbitros.arbnet.domain.universal.Equipment

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
