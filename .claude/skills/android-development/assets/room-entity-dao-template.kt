// Room Database Template with Entity, DAO, and Migrations
// Pattern: Type-safe queries, coroutines, Flow

package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Entity Definition
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val id: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "display_name")
    val displayName: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)

// DAO Interface
@Dao
interface UserDao {
    // Query with Flow (reactive)
    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY created_at DESC")
    fun observeActiveUsers(): Flow<List<UserEntity>>

    // Query with suspend (one-time)
    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    // Update
    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET is_active = :isActive WHERE user_id = :userId")
    suspend fun updateUserActiveStatus(userId: String, isActive: Boolean)

    // Delete
    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE user_id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    // Count
    @Query("SELECT COUNT(*) FROM users WHERE is_active = 1")
    suspend fun getActiveUserCount(): Int
}

// Database Definition
@Database(
    entities = [UserEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "app_database.db"
    }
}

// Database Provider (Hilt Module)
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration() // Remove in production
            .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    // Migration Example: Adding is_active column
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE users ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1")
        }
    }
}
