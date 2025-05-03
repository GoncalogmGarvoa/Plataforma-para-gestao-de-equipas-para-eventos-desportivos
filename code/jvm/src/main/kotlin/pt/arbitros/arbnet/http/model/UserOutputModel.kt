package pt.arbitros.arbnet.http.model

class UserOutputModel(
    val id: Int,
    val phoneNumber: String,
    val address: String,
    val name: String,
    val email: String,
    val birthDate: String,
    val iban: String,
    val roles: List<String>,
)
