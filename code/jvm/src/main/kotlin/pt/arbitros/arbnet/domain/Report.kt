package pt.arbitros.arbnet.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "Reports")
data class Report(
    @Id val id: String? = null,
    val reportType: String,
    val competitionId: Int
)
