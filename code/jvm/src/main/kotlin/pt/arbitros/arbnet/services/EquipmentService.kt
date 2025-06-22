package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.domain.universal.Equipment
import pt.arbitros.arbnet.http.model.EquipmentSelectModel
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo

sealed class EquipmentError {
    data object NotFound : EquipmentError()
    data object InternalError : EquipmentError()
}

@Component
class EquipmentService (
        @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
        private val utilsDomain: UtilsDomain,
    ) {

    fun getEquipment(): Either<EquipmentError, List<Equipment>> {
        return transactionManager.run {
            val equipment = it.equipmentRepository.getAllEquipment()
            if (equipment.isEmpty()) {
                return@run failure(EquipmentError.NotFound) //TODO verify this error
            }
            return@run success(equipment)
        }
    }

    fun selectEquipment(equipmentSelectModel: EquipmentSelectModel): Either<EquipmentError, Boolean> {
        return transactionManager.run {
            val equipmentRepository = it.equipmentRepository

            if(equipmentSelectModel.equipmentsId.isEmpty())
                return@run failure(EquipmentError.NotFound) //TODO verify this error

            if (it.competitionRepository.getCompetitionById(equipmentSelectModel.competitionId) == null) {
                return@run failure(EquipmentError.NotFound) //TODO verify this error
            }

            if (!equipmentRepository.verifyEquipmentId(equipmentSelectModel.equipmentsId))
                return@run failure(EquipmentError.NotFound) //TODO verify this error

            return@run success(
                equipmentRepository.selectEquipment(
                    competitionId = equipmentSelectModel.competitionId,
                    equipmentsId = equipmentSelectModel.equipmentsId
                )
            )
        }
    }

}