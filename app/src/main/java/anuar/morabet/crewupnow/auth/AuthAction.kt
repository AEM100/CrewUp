package anuar.morabet.crewupnow.auth

sealed interface AuthAction {

    data class Login(
        val email: String,
        val password: String
    ) : AuthAction

    data class Register(
        val name: String,
        val email: String,
        val password: String,
        val confirmPassword: String
    ) : AuthAction

    data object ShowLogin : AuthAction

    data object ShowRegister : AuthAction
}