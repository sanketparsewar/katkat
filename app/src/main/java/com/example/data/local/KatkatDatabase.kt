package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    ProfileEntity::class,
    UserProfileEntity::class,
    SwipeRecordEntity::class,
    ChatMessageEntity::class,
    SubscriptionEntity::class,
    AppNotificationEntity::class
  ],
  version = 7,
  exportSchema = false
)
abstract class KatkatDatabase : RoomDatabase() {
  abstract fun datingDao(): DatingDao

  companion object {
    @Volatile
    private var INSTANCE: KatkatDatabase? = null

    fun getDatabase(context: Context): KatkatDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          KatkatDatabase::class.java,
          "katkat_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
