package anuar.morabet.crewupnow.login

sealed interface LoginAction {

    data class Login(
        val email: String,
        val password: String
    ) : LoginAction

    data class Register(
        val email: String,
        val password: String
    ) : LoginAction
}