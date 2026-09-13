package com.ikogetech.ikogemind.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 2, // bumped for MessageEntity.feedback (thumbs up/down toolbar action)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ikogemind.db"
            )
                // Personal-testing scope (decisions-log.md) — no installed base to
                // preserve, so a destructive migration is fine instead of writing a
                // real Migration for this one added column. Revisit once this ships
                // beyond personal testing.
                .fallbackToDestructiveMigration()
                .build()
    }
}
