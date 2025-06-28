package pt.arbitros.arbnet.repository.adaptable_repos

import pt.arbitros.arbnet.domain.adaptable.Equipment

interface EquipmentRepository {

    fun getAllEquipment(): List<Equipment>

    fun selectEquipment(competitionId: Int, equipmentsId : List<Int>): Boolean

    fun verifyEquipmentIds(equipmentsId : List<Int>): Boolean

    fun getEquipmentByCompetitionId(competitionId: Int): List<Equipment>

    fun deleteEquipmentByCompetitionId(competitionId: Int): Boolean
}