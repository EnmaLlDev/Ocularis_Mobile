package fp.practices.ocularis_mobile.util

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import retrofit2.HttpException

/**
 * Centraliza el mapeo de errores tecnicos a mensajes claros para el usuario.
 */
object ErrorMessageMapper {
    fun fromThrowable(throwable: Throwable, operation: String? = null): String {
        return when (throwable) {
            is HttpException -> fromHttpException(throwable, operation)
            is SocketTimeoutException -> "El servidor tarda demasiado en responder. Intenta de nuevo."
            is ConnectException -> "No se pudo conectar con el servidor. Revisa tu conexion."
            is IOException -> "Ocurrio un problema de red. Intenta de nuevo."
            else -> operation?.let { "No se pudo $it. Intenta mas tarde." }
                ?: "Ocurrio un problema inesperado. Intenta de nuevo."
        }
    }

    private fun fromHttpException(exception: HttpException, operation: String?): String {
        return when (exception.code()) {
            400 -> "Datos invalidos. Revisa el formulario."
            401 -> "Tu sesion ha expirado. Inicia sesion de nuevo."
            403 -> operation?.let { "No tienes permisos para $it." }
                ?: "No tienes permisos para realizar esta accion."
            404 -> "No encontramos la informacion solicitada."
            409 -> "Ya existe un registro con esos datos."
            422 -> "Hay campos con formato invalido. Revisa la informacion."
            500 -> "Tenemos un problema con el servidor. Intenta mas tarde."
            else -> operation?.let { "No se pudo $it. Intenta de nuevo." }
                ?: "No se pudo completar la operacion. Intenta de nuevo."
        }
    }
}

