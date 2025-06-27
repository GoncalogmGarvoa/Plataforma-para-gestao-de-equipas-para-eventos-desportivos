package pt.arbitros.arbnet.http.model

import pt.arbitros.arbnet.domain.MatchDay
import pt.arbitros.arbnet.domain.Participant
import java.time.LocalDate

class EventOutputModel(
    val competitionName: String,
    val address: String,
    val phoneNumber: String,
    val email: String,
    val association: String,
    val location: String,
    val userId: Int,
    val participants: List<ParticipantWithCategory>?,
    val deadline: LocalDate,
    val callListType: String,
    val matchDaySessions: List<MatchDay>,
    val equipments: List<EquipmentOutputModel>,
)

class ParticipantWithCategory(
    val callListId: Int,
    val matchDayId: Int,
    val competitionIdMatchDay: Int,
    val userId: Int,
    val userName: String,
    val functionId: Int,
    val confirmationStatus: String,
    val category: String
)

class EquipmentOutputModel(
    val id: Int,
    val name: String,
)