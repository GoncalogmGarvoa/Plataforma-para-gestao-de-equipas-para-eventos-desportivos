package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.adaptable.Equipment
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo


@Component
class EquipmentService(@Qualifier(transactionRepo) private val transactionManager: TransactionManager)
{

    fun getAllEquipment(): Either<ApiError, List<Equipment>> =
        transactionManager.run {
            val equipments = it.equipmentRepository.getAllEquipment()
            if (equipments.isEmpty()) {
                failure(ApiError.NotFound("No equipments found", "There are no equipments registered"))
            } else {
                success(equipments)
            }
        }

}