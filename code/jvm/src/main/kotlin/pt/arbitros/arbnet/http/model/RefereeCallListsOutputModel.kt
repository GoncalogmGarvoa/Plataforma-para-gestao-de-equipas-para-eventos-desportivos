package pt.arbitros.arbnet.http.model

import pt.arbitros.arbnet.domain.MatchDay
import pt.arbitros.arbnet.domain.Participant
import pt.arbitros.arbnet.domain.Session
import pt.arbitros.arbnet.http.model.calllist.EquipmentOutputModel
import java.time.LocalDate

class RefereeCallListsOutputModel(
    val callListId: Int,
    val competitionName: String,
    val address: String,
    val phoneNumber: String,
    val email: String,
    val association: String,
    val location: String,
    val deadline: String,
    val callListType: String,
    val participants: List<ParticipantInfo>,
    val matchDaySessions: List<MatchDay>,
    val equipments: List<EquipmentOutputModel>,
)

class ParticipantInfo(
    val name: String,
    val category: String,
    val function: String,
    val status: String,
    val userId: Int,
    val matchDayId: Int
)