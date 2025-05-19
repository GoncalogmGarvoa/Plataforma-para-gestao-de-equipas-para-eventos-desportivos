package pt.arbitros.arbnet.domain.users

interface TokenEncoder {
    fun createValidationInformation(token: String): TokenValidationInfo
}
