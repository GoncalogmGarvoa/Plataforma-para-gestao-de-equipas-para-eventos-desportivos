package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.domain.adaptable.Equipment
import pt.arbitros.arbnet.repository.EquipmentRepository
import pt.arbitros.arbnet.repository.MatchDayRepository




class EquipmentRepositoryJdbi(
    private val handle: Handle,
) : EquipmentRepository {

    override fun getAllEquipment(): List<Equipment> =
        handle
            .createQuery("SELECT id, name FROM dbp.equipment")
            .mapTo(Equipment::class.java)
            .list()

}
