package pt.arbitros.arbnet.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalTime

@Document(collection = "CallLists")
data class CallListDocument(
    @Id val _id: String? = null,
    val sqlId: Int,
    val deadline: LocalDate,
    val callType: String,
    val userId: Int,
    val competition: CompetitionInfo,
    val matchDays: List<MatchDayEntry>
)

data class CompetitionInfo(
    val competitionNumber: Int,
    val name: String,
    val location: String,
    val address: String,
    val association: String,
    val email: String,
    val phoneNumber: String,
)

data class MatchDayEntry(
    val matchDayId: Int,
    val matchDate: LocalDate,
    val sessions: List<SessionInfo>,
    val participants: List<ParticipantWithFunction>
)

data class SessionInfo(
    val sessionId: Int,
    val startTime: LocalTime
)

data class ParticipantWithFunction(
    val userId: Int,
    val category: String,
    val function: String
)
