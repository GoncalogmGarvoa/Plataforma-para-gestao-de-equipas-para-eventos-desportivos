package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.universal.Equipment

interface EquipmentRepository {

    fun getEquipment(): List<Equipment>

    fun selectEquipment(competitionId: Int, equipmentsId : List<Int>): Boolean

    fun verifyEquipmentId(equipmentsId : List<Int>): Boolean
}