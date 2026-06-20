package parcours.android.eventorias.domain.exceptions

class UserNotFoundException(message: String) : Exception()
class DatabaseException(message: String) : Exception()
class NetworkException(message: String) : Exception()
class AuthException(message: String) : Exception()