package pt.arbitros.arbnet.domain

data class Competition(
    val competitionNumber: Int,
    val name: String,
    val address: String,
    val email: String,
    val phoneNumber: String,
    val location: String,
    val association: String,
)
