package com.otlub.sdk

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * OTLUB Android SDK
 * Easy integration with OTLUB API services for Android applications
 */
class OTLUBSDK private constructor(
    private val context: Context,
    private val config: Config
) {
    
    private val retrofit: Retrofit
    private val apiService: OTLApiService
    private val prefs: SharedPreferences
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    init {
        prefs = context.getSharedPreferences("otlub_sdk", Context.MODE_PRIVATE)
        
        val client = OkHttpClient.Builder()
            .connectTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
            .apply {
                if (config.debug) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()
        
        retrofit = Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(OTLApiService::class.java)
    }
    
    companion object {
        @Volatile
        private var INSTANCE: OTLUBSDK? = null
        
        fun initialize(context: Context, config: Config): OTLUBSDK {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OTLUBSDK(context, config).also { INSTANCE = it }
            }
        }
        
        fun getInstance(): OTLUBSDK {
            return INSTANCE ?: throw IllegalStateException("OTLUBSDK not initialized. Call initialize() first.")
        }
    }
    
    // Authentication
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                saveToken(loginResponse.token)
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(request: RegisterRequest): Result<User> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return try {
            val response = apiService.logout()
            clearToken()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // User Management
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to get user: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUser(user: UpdateUserRequest): Result<User> {
        return try {
            val response = apiService.updateUser(user)
            if (response.isSuccessful) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to update user: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Products
    suspend fun getProducts(params: Map<String, String> = emptyMap()): Result<List<Product>> {
        return try {
            val response = apiService.getProducts(params)
            if (response.isSuccessful) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to get products: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProduct(id: String): Result<Product> {
        return try {
            val response = apiService.getProduct(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to get product: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createProduct(product: CreateProductRequest): Result<Product> {
        return try {
            val response = apiService.createProduct(product)
            if (response.isSuccessful) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to create product: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Orders
    suspend fun getOrders(params: Map<String, String> = emptyMap()): Result<List<Order>> {
        return try {
            val response = apiService.getOrders(params)
            if (response.isSuccessful) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to get orders: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createOrder(order: CreateOrderRequest): Result<Order> {
        return try {
            val response = apiService.createOrder(order)
            if (response.isSuccessful) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to create order: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Stock
    suspend fun getStock(productId: String): Result<StockInfo> {
        return try {
            val response = apiService.getStock(productId)
            if (response.isSuccessful) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to get stock: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // E-Wallet
    suspend fun getWallet(): Result<Wallet> {
        return try {
            val response = apiService.getWallet()
            if (response.isSuccessful) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to get wallet: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTransactions(params: Map<String, String> = emptyMap()): Result<List<Transaction>> {
        return try {
            val response = apiService.getTransactions(params)
            if (response.isSuccessful) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to get transactions: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Health Check
    suspend fun healthCheck(): Result<HealthResponse> {
        return try {
            val response = apiService.healthCheck()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Health check failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Token Management
    private fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }
    
    private fun getToken(): String? {
        return prefs.getString("auth_token", null)
    }
    
    private fun clearToken() {
        prefs.edit().remove("auth_token").apply()
    }
    
    fun isAuthenticated(): Boolean {
        return getToken() != null
    }
    
    fun getStoredToken(): String? {
        return getToken()
    }
    
    fun destroy() {
        coroutineScope.cancel()
    }
    
    // Configuration
    data class Config(
        val baseUrl: String = "https://router.snapone.studio",
        val apiKey: String? = null,
        val timeoutSeconds: Long = 30,
        val debug: Boolean = false
    )
}

// API Service Interface
interface OTLApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): retrofit2.Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): retrofit2.Response<ApiResponse<User>>
    
    @POST("auth/logout")
    suspend fun logout(): retrofit2.Response<Unit>
    
    @GET("user/me")
    suspend fun getCurrentUser(): retrofit2.Response<ApiResponse<User>>
    
    @PUT("user/me")
    suspend fun updateUser(@Body user: UpdateUserRequest): retrofit2.Response<ApiResponse<User>>
    
    @GET("products")
    suspend fun getProducts(@QueryMap params: Map<String, String>): retrofit2.Response<ApiResponse<List<Product>>>
    
    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): retrofit2.Response<ApiResponse<Product>>
    
    @POST("products")
    suspend fun createProduct(@Body product: CreateProductRequest): retrofit2.Response<ApiResponse<Product>>
    
    @GET("orders")
    suspend fun getOrders(@QueryMap params: Map<String, String>): retrofit2.Response<ApiResponse<List<Order>>>
    
    @POST("orders")
    suspend fun createOrder(@Body order: CreateOrderRequest): retrofit2.Response<ApiResponse<Order>>
    
    @GET("stock/{productId}")
    suspend fun getStock(@Path("productId") productId: String): retrofit2.Response<ApiResponse<StockInfo>>
    
    @GET("ewallet")
    suspend fun getWallet(): retrofit2.Response<ApiResponse<Wallet>>
    
    @GET("ewallet/transactions")
    suspend fun getTransactions(@QueryMap params: Map<String, String>): retrofit2.Response<ApiResponse<List<Transaction>>>
    
    @GET("health")
    suspend fun healthCheck(): retrofit2.Response<HealthResponse>
}

// Data Classes
data class ApiResponse<T>(
    val data: T,
    val status: Int,
    val message: String?,
    val timestamp: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User,
    val expiresAt: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null
)

data class UpdateUserRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val avatar: String? = null
)

data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val avatar: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String,
    val category: Category,
    val attributes: List<ProductAttribute>,
    val images: List<String>,
    val stock: StockInfo,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class Category(
    val id: String,
    val name: String,
    val description: String?,
    val parentId: String?,
    val children: List<Category>?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class ProductAttribute(
    val name: String,
    val value: String,
    val type: String
)

data class StockInfo(
    val quantity: Int,
    val reserved: Int,
    val available: Int,
    val warehouse: Warehouse
)

data class Warehouse(
    val id: String,
    val name: String,
    val address: Address,
    val capacity: Int,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val country: String,
    val postalCode: String,
    val coordinates: Coordinates?
)

data class Coordinates(
    val lat: Double,
    val lng: Double
)

data class Order(
    val id: String,
    val userId: String,
    val items: List<OrderItem>,
    val total: Double,
    val currency: String,
    val status: String,
    val shippingAddress: Address,
    val billingAddress: Address,
    val paymentMethod: String,
    val createdAt: String,
    val updatedAt: String
)

data class OrderItem(
    val productId: String,
    val quantity: Int,
    val price: Double,
    val total: Double
)

data class CreateProductRequest(
    val name: String,
    val description: String,
    val price: Double,
    val currency: String,
    val categoryId: String,
    val attributes: List<ProductAttribute> = emptyList(),
    val images: List<String> = emptyList()
)

data class CreateOrderRequest(
    val items: List<OrderItem>,
    val shippingAddress: Address,
    val billingAddress: Address,
    val paymentMethod: String
)

data class Wallet(
    val id: String,
    val userId: String,
    val balance: Double,
    val currency: String,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class Transaction(
    val id: String,
    val walletId: String,
    val type: String,
    val amount: Double,
    val currency: String,
    val description: String,
    val status: String,
    val createdAt: String
)

data class HealthResponse(
    val status: String,
    val timestamp: String,
    val router: String,
    val version: String
)
