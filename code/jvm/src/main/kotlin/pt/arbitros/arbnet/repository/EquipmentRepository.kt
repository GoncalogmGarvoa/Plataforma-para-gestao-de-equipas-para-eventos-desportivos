package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.adaptable.Equipment


interface EquipmentRepository {

    fun getAllEquipment(
    ): List<Equipment>

}