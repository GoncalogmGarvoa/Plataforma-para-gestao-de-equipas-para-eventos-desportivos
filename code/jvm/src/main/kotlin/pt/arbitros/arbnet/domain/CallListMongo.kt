package pt.arbitros.arbnet.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "CallLists")
data class CallListDocument(
    @Id val _id: String? = null,              // MongoDB document ID
    val sqlId: Int,                  // SQL call_list.id
    val deadline: LocalDate,         // e.g. "2025-06-01"
    val callType: String, // "callList", "sealedCallList", etc.
    val userId: Int,
    val competition: CompetitionInfo,
    val participants: List<ParticipantEntry>
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

data class ParticipantEntry(
    val userId: Int,
    val name: String,
    val category: String,
    val assignments: List<MatchAssignment>
)

data class MatchAssignment(
    val matchDayId: Int,
    val matchDate: LocalDate,
    val function: String
)