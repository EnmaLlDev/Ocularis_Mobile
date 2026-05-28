package fp.practices.ocularis_mobile.data.repository

import fp.practices.ocularis_mobile.data.model.DashboardVisualContent
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.data.network.RetrofitClient

class DashboardVisualRepository(api: ApiService? = null) {
    private val api: ApiService = api ?: RetrofitClient.requireAuthApiService()

    suspend fun getVisualContent(): DashboardVisualContent {
        return runCatching {
            api.getDashboardVisuals()
        }.getOrElse {
            DashboardVisualContent.default()
        }
    }
}

