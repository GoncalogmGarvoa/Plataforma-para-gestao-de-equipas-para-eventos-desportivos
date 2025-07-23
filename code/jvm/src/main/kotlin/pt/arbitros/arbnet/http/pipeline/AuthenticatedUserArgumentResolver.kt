package pt.arbitros.arbnet.domain.users

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthenticatedUserArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == AuthenticatedUser::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val request =
            webRequest.getNativeRequest(HttpServletRequest::class.java)
                ?: throw IllegalStateException("Could not extract HttpServletRequest from NativeWebRequest.")
        return getUserFrom(request) ?:
        throw IllegalStateException("Missing AuthenticatedUser in request. Ensure it was set before controller execution.")
    }

    companion object {
        private const val KEY = "AuthenticatedUserArgumentResolver"

        fun addUserTo(
            user: AuthenticatedUser,
            request: HttpServletRequest,
        ) = request.setAttribute(KEY, user)

        fun getUserFrom(request: HttpServletRequest): AuthenticatedUser? =
            request.getAttribute(KEY)?.let {
                it as? AuthenticatedUser
            }
    }
}
