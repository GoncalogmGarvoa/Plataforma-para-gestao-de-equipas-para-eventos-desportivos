package pt.arbitros.arbnet.http.model

class UserOutputModel(
    val id: Int,
    val phoneNumber: Int,
    val address: String,
    val name: String,
    val email: String,
    val birthDate: String,
    val iban: String,
    val roles: List<String>,
)
// TODO: check if we send back iban ,review class
