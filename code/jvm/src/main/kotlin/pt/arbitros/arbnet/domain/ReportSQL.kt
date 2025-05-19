package pt.arbitros.arbnet.domain

import org.springframework.data.annotation.Id

class ReportSQL(
    val id: String,
    val reportType: String,
    val competitionId: Int
)