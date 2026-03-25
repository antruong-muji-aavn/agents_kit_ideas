// Hilt Dependency Injection Module Template
// Pattern: Repository bindings, use case provision, scoped dependencies

package com.example.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Application-Level Module (Singleton)
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppContext(
        @ApplicationContext context: Context
    ): Context = context

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

// Data Module (Singleton)
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    // Bind Repository Implementation
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository
}

// Repository Implementations (Example)
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val apiService: ApiService
) : UserRepository {
    override suspend fun getUsers(): List<User> {
        // Implementation combining local and remote data
        return emptyList()
    }
}

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val apiService: ApiService
) : ProductRepository {
    override suspend fun getProducts(): List<Product> {
        return emptyList()
    }
}

// Domain Module (ViewModel Scoped)
@Module
@InstallIn(ViewModelComponent::class)
object DomainModule {

    @Provides
    @ViewModelScoped
    fun provideGetUsersUseCase(
        repository: UserRepository
    ): GetUsersUseCase {
        return GetUsersUseCase(repository)
    }

    @Provides
    @ViewModelScoped
    fun provideDeleteUserUseCase(
        repository: UserRepository
    ): DeleteUserUseCase {
        return DeleteUserUseCase(repository)
    }
}

// Use Case Examples
class GetUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<List<User>> {
        return try {
            Result.success(repository.getUsers())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class DeleteUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return try {
            repository.deleteUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Qualifier Annotations (for multiple implementations)
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalDataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteDataSource

// Example: Multiple Data Sources
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    @LocalDataSource
    abstract fun bindLocalDataSource(
        impl: LocalDataSourceImpl
    ): DataSource

    @Binds
    @Singleton
    @RemoteDataSource
    abstract fun bindRemoteDataSource(
        impl: RemoteDataSourceImpl
    ): DataSource
}

// Using Qualifiers in Repository
class QualifiedRepositoryImpl @Inject constructor(
    @LocalDataSource private val localSource: DataSource,
    @RemoteDataSource private val remoteSource: DataSource
) : Repository {
    // Implementation using both sources
}

// Dispatcher Provider (for testing)
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DispatcherModule {
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(
        impl: DefaultDispatcherProvider
    ): DispatcherProvider
}
