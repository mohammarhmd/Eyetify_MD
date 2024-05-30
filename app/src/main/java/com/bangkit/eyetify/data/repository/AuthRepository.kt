package com.bangkit.eyetify.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bangkit.eyetify.data.model.UserModel
import com.bangkit.eyetify.data.preference.UserPreference
import com.bangkit.eyetify.data.response.LoginResponse
import com.bangkit.eyetify.data.response.RegisterResponse
import com.bangkit.eyetify.data.retrofit.AuthService
import com.bangkit.eyetify.data.preference.Result
import com.bangkit.eyetify.data.response.PostResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class AuthRepository(
    private val authService: AuthService,
    private val userPreferences: UserPreference
) {
    private val _registerResponse = MutableLiveData<RegisterResponse>()
    val registerResponse: LiveData<RegisterResponse> = _registerResponse
    private val _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse: LiveData<LoginResponse> = _loginResponse

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<RegisterResponse> {
        return try {
            val response = authService.register(name, email, password)
            if (!response.error) {
                Result.DataSuccess(response)
            } else {
                Result.DataError(response.message)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, PostResponse::class.java)
            Result.DataError(errorResponse.message)
        } catch (e: Exception) {
            Result.DataError("Registration failed")
        }
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = authService.login(email, password)
            if (!response.error) {
                Result.DataSuccess(response)
            } else {
                Result.DataError(response.message)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, PostResponse::class.java)
            Result.DataError(errorResponse.message)
        } catch (e: Exception) {
            Result.DataError("Login failed")
        }
    }

    suspend fun saveLoginSession(user: UserModel) {
        userPreferences.saveLoginSession(user)
    }

    fun getLoginSession(): Flow<UserModel> {
        return userPreferences.getLoginSession()
    }

    suspend fun logout() {
        userPreferences.logoutSession()
    }

    companion object {
        fun getInstance(
            authApiService: AuthService,
            userPreference: UserPreference) = AuthRepository(authApiService, userPreference)
    }
}