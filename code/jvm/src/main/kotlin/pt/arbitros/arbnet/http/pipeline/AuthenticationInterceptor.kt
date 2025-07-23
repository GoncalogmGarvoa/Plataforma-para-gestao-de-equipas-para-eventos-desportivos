package pt.arbitros.arbnet.http.pipeline

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import pt.arbitros.arbnet.domain.users.AuthenticatedUserArgumentResolver
import pt.arbitros.arbnet.domain.users.RequestTokenProcessor
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.Problem
import pt.arbitros.arbnet.http.Uris

@Component
class AuthenticationInterceptor(
    private val authorizationHeaderProcessor: RequestTokenProcessor,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {

        if (request.requestURI in Uris.NoAuthUris)
            return true // skip authentication for no-auth URIs

        // enforce authentication
        val user = authorizationHeaderProcessor
                .processAuthorizationHeaderValue(request.getHeader(NAME_AUTHORIZATION_HEADER))
        return if (user == null) {

            val error = ApiError.Unauthorized()
            val problem = Problem.fromApiErrorToProblemResponse(error)

            response.status = 401
            response.contentType = "application/problem+json"
            response.setHeader("date", java.time.Instant.now().toString())
            val mapper = ObjectMapper()
            mapper.writeValue(response.outputStream, problem)

            response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
            false
        } else {
            AuthenticatedUserArgumentResolver.addUserTo(user, request)
            true
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)
        const val NAME_AUTHORIZATION_HEADER = "Authorization"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}
