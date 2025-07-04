package pt.arbitros.arbnet.http.model.users

class UserCreationInputModel(
    val name: String,
    val phoneNumber: String,
    val address: String,
    val email: String,
    val password: String,
    val birthDate: String,
    val iban: String,
    val creationToken : String? = null,
)