package fp.practices.ocularis_mobile.data.network

import fp.practices.ocularis_mobile.data.model.AppointmentDTO
import fp.practices.ocularis_mobile.data.model.DetailsDTO
import fp.practices.ocularis_mobile.data.model.DoctorDTO
import fp.practices.ocularis_mobile.data.model.PatientDTO
import fp.practices.ocularis_mobile.data.model.auth.AuthResponse
import fp.practices.ocularis_mobile.data.model.auth.LoginRequest
import fp.practices.ocularis_mobile.data.model.auth.MeResponse
import fp.practices.ocularis_mobile.data.model.auth.RefreshRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse

    @GET("/auth/me")
    suspend fun me(): MeResponse

    @POST("/auth/logout")
    suspend fun logout(@Body request: RefreshRequest? = null): Response<Unit>

    @GET("/api/patient/getAll")
    suspend fun getPatients(): List<PatientDTO>

    @GET("/api/patient/get/{id}")
    suspend fun getPatient(@Path("id") id: Int): PatientDTO

    @POST("/api/patient/create")
    suspend fun createPatient(@Body patient: PatientDTO): PatientDTO

    @PUT("/api/patient/update/{id}")
    suspend fun updatePatient(@Path("id") id: Int, @Body patient: PatientDTO): Response<Unit>

    @DELETE("/api/patient/delete/{id}")
    suspend fun deletePatient(@Path("id") id: Int): Response<Unit>

    @GET("/api/patient/address/{address}")
    suspend fun searchPatientsByAddress(@Path("address") address: String): List<PatientDTO>

    @GET("/api/doctor/getAll")
    suspend fun getDoctors(): List<DoctorDTO>

    @GET("/api/doctor/{id}")
    suspend fun getDoctor(@Path("id") id: Int): DoctorDTO

    @POST("/api/doctor/create")
    suspend fun createDoctor(@Body doctor: DoctorDTO): DoctorDTO

    @PUT("/api/doctor/update/{id}")
    suspend fun updateDoctor(@Path("id") id: Int, @Body doctor: DoctorDTO): Response<Unit>

    @DELETE("/api/doctor/{id}")
    suspend fun deleteDoctor(@Path("id") id: Int): Response<Unit>

    @GET("/api/doctor/license/{licenseNumber}")
    suspend fun searchDoctorsByLicense(@Path("licenseNumber") licenseNumber: String): List<DoctorDTO>

    @GET("/api/doctor/search/{terms}")
    suspend fun searchDoctorsBySpecialty(@Path("terms") terms: String): List<DoctorDTO>

    @GET("/api/appointment/getAll")
    suspend fun getAppointments(): List<AppointmentDTO>

    @GET("/api/appointment/{id}")
    suspend fun getAppointment(@Path("id") id: Int): AppointmentDTO

    @POST("/api/appointment/create")
    suspend fun createAppointment(@Body appointment: AppointmentDTO): AppointmentDTO

    @PUT("/api/appointment/update/{id}")
    suspend fun updateAppointment(@Path("id") id: Int, @Body appointment: AppointmentDTO): Response<Unit>

    @DELETE("/api/appointment/delete/{id}")
    suspend fun deleteAppointment(@Path("id") id: Int): Response<Unit>

    @GET("/api/details/getAll")
    suspend fun getDetails(): List<DetailsDTO>

    @GET("/api/details/{id}")
    suspend fun getDetail(@Path("id") id: Int): DetailsDTO

    @GET("/api/details/appointment/{appointmentId}")
    suspend fun getDetailsByAppointment(@Path("appointmentId") appointmentId: Int): List<DetailsDTO>

    @POST("/api/details/create")
    suspend fun createDetail(@Body detail: DetailsDTO): DetailsDTO

    @PUT("/api/details/update/{id}")
    suspend fun updateDetail(@Path("id") id: Int, @Body detail: DetailsDTO): Response<Unit>

    @DELETE("/api/details/delete/{id}")
    suspend fun deleteDetail(@Path("id") id: Int): Response<Unit>

}

