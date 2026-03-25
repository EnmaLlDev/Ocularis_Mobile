package fp.practices.ocularis_mobile.data.repository
import fp.practices.ocularis_mobile.data.model.DetailsDTO
import fp.practices.ocularis_mobile.data.network.ApiService
import fp.practices.ocularis_mobile.data.network.RetrofitClient
class DetailsRepository(private val api: ApiService = RetrofitClient.requireApiService()) {
    suspend fun getDetails(): List<DetailsDTO> = api.getDetails()
    suspend fun getDetail(id: Int): DetailsDTO = api.getDetail(id)
    suspend fun getByAppointment(appointmentId: Int): List<DetailsDTO> = api.getDetailsByAppointment(appointmentId)
    suspend fun create(detail: DetailsDTO): DetailsDTO = api.createDetail(detail)
    suspend fun update(id: Int, detail: DetailsDTO): Boolean = api.updateDetail(id, detail).isSuccessful
    suspend fun delete(id: Int): Boolean = api.deleteDetail(id).isSuccessful
}
