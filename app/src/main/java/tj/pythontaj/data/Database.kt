package tj.pythontaj.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(@PrimaryKey val lessonId: String, val completedAt: Long)

@Entity(tableName = "exercise_attempts")
data class ExerciseAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: String,
    val lessonId: String,
    val passed: Boolean,
    val code: String,
    val createdAt: Long,
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lessonId: String,
    val score: Int,
    val total: Int,
    val createdAt: Long,
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(@PrimaryKey val lessonId: String, val createdAt: Long)

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val code: String,
    val createdAt: Long,
)

@Entity(tableName = "study_days")
data class StudyDayEntity(@PrimaryKey val epochDay: Long)

@Dao
interface LearningDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertLesson(e: LessonProgressEntity)
    @Query("SELECT lessonId FROM lesson_progress") fun completedLessonIds(): Flow<List<String>>

    @Insert suspend fun insertAttempt(e: ExerciseAttemptEntity)
    @Query("SELECT DISTINCT exerciseId FROM exercise_attempts WHERE passed = 1") fun passedExerciseIds(): Flow<List<String>>
    @Query("SELECT DISTINCT exerciseId FROM exercise_attempts WHERE passed = 1") suspend fun passedExerciseIdsOnce(): List<String>
    @Query("SELECT * FROM exercise_attempts WHERE exerciseId = :id ORDER BY createdAt DESC") fun attemptsFor(id: String): Flow<List<ExerciseAttemptEntity>>

    @Insert suspend fun insertQuiz(e: QuizResultEntity)
    @Query("SELECT * FROM quiz_results ORDER BY createdAt DESC") fun quizResults(): Flow<List<QuizResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun addBookmark(e: BookmarkEntity)
    @Query("DELETE FROM bookmarks WHERE lessonId = :id") suspend fun removeBookmark(id: String)
    @Query("SELECT lessonId FROM bookmarks") fun bookmarkIds(): Flow<List<String>>

    @Insert suspend fun insertSnippet(e: SnippetEntity)
    @Query("DELETE FROM snippets WHERE id = :id") suspend fun deleteSnippet(id: Long)
    @Query("SELECT * FROM snippets ORDER BY createdAt DESC") fun snippets(): Flow<List<SnippetEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addStudyDay(e: StudyDayEntity)
    @Query("SELECT epochDay FROM study_days") fun studyDays(): Flow<List<Long>>
}

/**
 * Нусхаи 1. Ҳангоми тағйири сохтор version-ро зиёд кунед ва Migration илова намоед
 * (ниг. docs/ARCHITECTURE.md). fallbackToDestructiveMigration ИСТИФОДА НАМЕШАВАД,
 * то пешрафти корбар ҳеҷ гоҳ нест нашавад.
 */
@Database(
    entities = [LessonProgressEntity::class, ExerciseAttemptEntity::class, QuizResultEntity::class,
        BookmarkEntity::class, SnippetEntity::class, StudyDayEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): LearningDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "pythontaj.db")
                .build().also { instance = it }
        }
    }
}
