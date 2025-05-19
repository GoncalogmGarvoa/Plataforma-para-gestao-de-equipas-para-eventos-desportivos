package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.domain.universal.Equipment
import pt.arbitros.arbnet.repository.EquipmentRepository

class EquipmentRepositoryJdbi(
    private val handle : Handle,
) : EquipmentRepository {

    override fun getEquipment(): List<Equipment> {
        val sql =  """ select * from dbp.equipment"""

        return handle.createQuery(sql)
            .mapTo(Equipment::class.java)
            .list()
    }

    override fun selectEquipment(competitionId: Int, equipmentsId: List<Int>): Boolean {
        val sql = """
        insert into dbp.competition_equipment (competition_id, equipment_id)
        values (:competitionId, :equipmentId)
        """

        val batch = handle.prepareBatch(sql)
        equipmentsId.forEach { equipmentId ->
            batch.bind("competitionId", competitionId)
                .bind("equipmentId", equipmentId)
                .add()
        }

        return batch.execute().isNotEmpty()
    }

    override fun verifyEquipmentId(equipmentsId: List<Int>): Boolean {
        val sql = """
        select count(*) from dbp.equipment
        where id in (<equipmentsId>)
        """

        val count = handle.createQuery(sql)
            .bindList("equipmentsId", equipmentsId)
            .mapTo(Int::class.java)
            .one()

        return count == equipmentsId.size
    }

}