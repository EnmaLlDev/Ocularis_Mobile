package fp.practices.ocularis_mobile.data.repository

import fp.practices.ocularis_mobile.data.model.Dashboard
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.data.network.RetrofitClient

/**
 * Repositorio que obtiene el contenido visual del dashboard.
 */
class DashboardVisualRepository(api: ApiService? = null) {
    private val api: ApiService = api ?: RetrofitClient.requireApiService()

    /**
     * Devuelve los datos del dashboard o los valores por defecto si falla.
     */
    suspend fun getVisualContent(): Dashboard {
        return runCatching {
            api.getDashboardVisuals()
        }.getOrElse {
            Dashboard.default()
        }
    }
}

