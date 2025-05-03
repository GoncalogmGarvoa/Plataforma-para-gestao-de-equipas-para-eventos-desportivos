package pt.arbitros.arbnet.repository.auxiliary

import pt.arbitros.arbnet.domain.Equipment

interface EquipmentRepository {
    fun createEquipment(equipment: Equipment): Int

    fun findEquipmentById(id: Int): Equipment?

    fun getAllEquipment(): List<Equipment>

    fun deleteEquipment(id: Int): Boolean
}
