// Retrofit Service Template with Interceptors and Error Handling
// Pattern: Kotlin Serialization, Response wrapper, logging

package com.example.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.*

// API Service Interface
interface ApiService {
    @GET("api/users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<UserDto>>>

    @GET("api/users/{id}")
    suspend fun getUserById(
        @Path("id") userId: String
    ): Response<ApiResponse<UserDto>>

    @POST("api/users")
    suspend fun createUser(
        @Body user: CreateUserRequest
    ): Response<ApiResponse<UserDto>>

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") userId: String,
        @Body user: UpdateUserRequest
    ): Response<ApiResponse<UserDto>>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Path("id") userId: String
    ): Response<ApiResponse<Unit>>
}

// Response Wrapper
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: ApiError? = null
)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)

// DTOs
@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val createdAt: Long
)

@Serializable
data class CreateUserRequest(
    val email: String,
    val displayName: String
)

@Serializable
data class UpdateUserRequest(
    val displayName: String? = null,
    val email: String? = null
)

// Network Module (Hilt)
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}

// Auth Interceptor
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Add authentication header
        val request = original.newBuilder()
            .addHeader("Authorization", "Bearer YOUR_TOKEN")
            .addHeader("Accept", "application/json")
            .build()

        return chain.proceed(request)
    }
}

// Result Wrapper for Safe API Calls
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: String? = null) : ApiResult<Nothing>()
}

// Extension for Safe API Calls
suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<ApiResponse<T>>
): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true && body.data != null) {
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(
                    message = body?.message ?: "Unknown error",
                    code = body?.error?.code
                )
            }
        } else {
            ApiResult.Error(
                message = "HTTP ${response.code()}: ${response.message()}",
                code = response.code().toString()
            )
        }
    } catch (e: Exception) {
        ApiResult.Error(
            message = e.message ?: "Network error",
            code = "NETWORK_ERROR"
        )
    }
}
