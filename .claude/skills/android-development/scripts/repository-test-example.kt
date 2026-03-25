// Repository Unit Test Example with Fake Data Sources
// Tests: Data layer logic, caching, error handling

package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.remote.ApiService
import com.example.data.remote.UserDto
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    private lateinit var repository: UserRepositoryImpl
    private lateinit var apiService: ApiService
    private lateinit var userDao: UserDao

    private val mockUserDto = UserDto(
        id = "1",
        email = "test@example.com",
        displayName = "Test User",
        createdAt = 1234567890L
    )

    private val mockUserEntity = UserEntity(
        id = "1",
        email = "test@example.com",
        displayName = "Test User",
        createdAt = 1234567890L,
        isActive = true
    )

    @Before
    fun setup() {
        apiService = mockk()
        userDao = mockk(relaxed = true) // Auto-mock Unit return types
        repository = UserRepositoryImpl(apiService, userDao)
    }

    @Test
    fun `getUsers success fetches from API and caches locally`() = runTest {
        // Given
        val userDtos = listOf(mockUserDto)
        coEvery { apiService.getUsers() } returns userDtos

        // When
        val result = repository.getUsers()

        // Then
        assertEquals(1, result.size)
        assertEquals("test@example.com", result.first().email)

        // Verify caching
        coVerify { userDao.insertUsers(any()) }
    }

    @Test
    fun `getUsers network failure returns cached data`() = runTest {
        // Given
        coEvery { apiService.getUsers() } throws IOException("Network error")
        coEvery { userDao.getUsers() } returns listOf(mockUserEntity)

        // When
        val result = repository.getUsers()

        // Then
        assertEquals(1, result.size)
        assertEquals("test@example.com", result.first().email)

        // Verify API was called but DAO fallback used
        coVerify { apiService.getUsers() }
        coVerify { userDao.getUsers() }
    }

    @Test
    fun `getUsers network failure with empty cache returns empty list`() = runTest {
        // Given
        coEvery { apiService.getUsers() } throws IOException("Network error")
        coEvery { userDao.getUsers() } returns emptyList()

        // When
        val result = repository.getUsers()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getUserById success returns user`() = runTest {
        // Given
        coEvery { apiService.getUserById("1") } returns mockUserDto

        // When
        val result = repository.getUserById("1")

        // Then
        assertNotNull(result)
        assertEquals("1", result?.id)
        assertEquals("test@example.com", result?.email)
    }

    @Test
    fun `getUserById not found returns null`() = runTest {
        // Given
        coEvery { apiService.getUserById("999") } throws IOException("Not found")
        coEvery { userDao.getUserById("999") } returns null

        // When
        val result = repository.getUserById("999")

        // Then
        assertNull(result)
    }

    @Test
    fun `createUser success saves to API and local DB`() = runTest {
        // Given
        val newUser = User(
            id = "2",
            email = "new@example.com",
            displayName = "New User"
        )
        val createdDto = UserDto(
            id = "2",
            email = "new@example.com",
            displayName = "New User",
            createdAt = System.currentTimeMillis()
        )
        coEvery { apiService.createUser(any()) } returns createdDto

        // When
        val result = repository.createUser(newUser)

        // Then
        assertNotNull(result)
        assertEquals("2", result?.id)

        // Verify both API and DB updated
        coVerify { apiService.createUser(any()) }
        coVerify { userDao.insertUser(any()) }
    }

    @Test
    fun `deleteUser removes from API and local DB`() = runTest {
        // Given
        coEvery { apiService.deleteUser("1") } just Runs

        // When
        repository.deleteUser("1")

        // Then
        coVerify { apiService.deleteUser("1") }
        coVerify { userDao.deleteUserById("1") }
    }

    @Test
    fun `observeUsers emits Flow from DAO`() = runTest {
        // Given
        val entities = listOf(mockUserEntity)
        coEvery { userDao.observeUsers() } returns flowOf(entities)

        // When
        val result = repository.observeUsers().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("test@example.com", result.first().email)
    }

    @Test
    fun `updateUserActiveStatus calls DAO`() = runTest {
        // Given
        val userId = "1"
        val isActive = false

        // When
        repository.updateUserActiveStatus(userId, isActive)

        // Then
        coVerify { userDao.updateUserActiveStatus(userId, isActive) }
    }

    @Test
    fun `multiple concurrent getUsers calls handled correctly`() = runTest {
        // Given
        coEvery { apiService.getUsers() } coAnswers {
            delay(100)
            listOf(mockUserDto)
        }

        // When - Make concurrent calls
        val deferred1 = async { repository.getUsers() }
        val deferred2 = async { repository.getUsers() }
        val deferred3 = async { repository.getUsers() }

        val results = listOf(
            deferred1.await(),
            deferred2.await(),
            deferred3.await()
        )

        // Then - All completed successfully
        assertEquals(3, results.size)
        results.forEach { result ->
            assertEquals(1, result.size)
        }
    }
}

// Alternative: Using Fake Data Sources (Preferred for Complex Logic)

class FakeUserDao : UserDao {
    private val users = mutableMapOf<String, UserEntity>()
    private val usersFlow = MutableStateFlow<List<UserEntity>>(emptyList())

    override suspend fun getUserById(userId: String): UserEntity? {
        return users[userId]
    }

    override suspend fun getUserByEmail(email: String): UserEntity? {
        return users.values.find { it.email == email }
    }

    override fun observeUsers(): Flow<List<UserEntity>> = usersFlow

    override suspend fun insertUser(user: UserEntity) {
        users[user.id] = user
        usersFlow.value = users.values.toList()
    }

    override suspend fun insertUsers(users: List<UserEntity>) {
        users.forEach { user ->
            this.users[user.id] = user
        }
        usersFlow.value = this.users.values.toList()
    }

    override suspend fun updateUser(user: UserEntity) {
        users[user.id] = user
        usersFlow.value = users.values.toList()
    }

    override suspend fun updateUserActiveStatus(userId: String, isActive: Boolean) {
        users[userId]?.let { user ->
            users[userId] = user.copy(isActive = isActive)
            usersFlow.value = users.values.toList()
        }
    }

    override suspend fun deleteUser(user: UserEntity) {
        users.remove(user.id)
        usersFlow.value = users.values.toList()
    }

    override suspend fun deleteUserById(userId: String) {
        users.remove(userId)
        usersFlow.value = users.values.toList()
    }

    override suspend fun deleteAllUsers() {
        users.clear()
        usersFlow.value = emptyList()
    }

    override suspend fun getActiveUserCount(): Int {
        return users.values.count { it.isActive }
    }

    // Test helpers
    fun getUsersSnapshot(): List<UserEntity> = users.values.toList()
}

class FakeApiService : ApiService {
    private val users = mutableListOf<UserDto>()
    var shouldThrowError = false
    var errorToThrow: Exception = IOException("Network error")

    override suspend fun getUsers(): List<UserDto> {
        if (shouldThrowError) throw errorToThrow
        return users.toList()
    }

    override suspend fun getUserById(userId: String): UserDto {
        if (shouldThrowError) throw errorToThrow
        return users.find { it.id == userId }
            ?: throw IOException("User not found")
    }

    override suspend fun createUser(user: CreateUserRequest): UserDto {
        if (shouldThrowError) throw errorToThrow
        val newUser = UserDto(
            id = "id_${users.size + 1}",
            email = user.email,
            displayName = user.displayName,
            createdAt = System.currentTimeMillis()
        )
        users.add(newUser)
        return newUser
    }

    override suspend fun updateUser(userId: String, user: UpdateUserRequest): UserDto {
        if (shouldThrowError) throw errorToThrow
        val index = users.indexOfFirst { it.id == userId }
        if (index == -1) throw IOException("User not found")

        val existingUser = users[index]
        val updatedUser = existingUser.copy(
            displayName = user.displayName ?: existingUser.displayName,
            email = user.email ?: existingUser.email
        )
        users[index] = updatedUser
        return updatedUser
    }

    override suspend fun deleteUser(userId: String) {
        if (shouldThrowError) throw errorToThrow
        users.removeIf { it.id == userId }
    }

    // Test helpers
    fun addUser(user: UserDto) {
        users.add(user)
    }
}

// Test with Fakes
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryWithFakesTest {

    private lateinit var repository: UserRepositoryImpl
    private lateinit var fakeApi: FakeApiService
    private lateinit var fakeDao: FakeUserDao

    @Before
    fun setup() {
        fakeApi = FakeApiService()
        fakeDao = FakeUserDao()
        repository = UserRepositoryImpl(fakeApi, fakeDao)
    }

    @Test
    fun `getUsers caches data correctly`() = runTest {
        // Given
        fakeApi.addUser(UserDto("1", "test@example.com", "Test", 123L))

        // When
        repository.getUsers()

        // Then - Data cached in DAO
        val cachedUsers = fakeDao.getUsersSnapshot()
        assertEquals(1, cachedUsers.size)
        assertEquals("test@example.com", cachedUsers.first().email)
    }

    @Test
    fun `getUsers with network error falls back to cache`() = runTest {
        // Given - Cache data first
        fakeDao.insertUser(UserEntity("1", "cached@example.com", "Cached", 123L, true))

        // Then set API to fail
        fakeApi.shouldThrowError = true

        // When
        val result = repository.getUsers()

        // Then - Returns cached data
        assertEquals(1, result.size)
        assertEquals("cached@example.com", result.first().email)
    }
}
