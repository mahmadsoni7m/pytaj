package tj.pythontaj

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tj.pythontaj.content.*
import tj.pythontaj.data.*
import tj.pythontaj.domain.*
import tj.pythontaj.execution.ChaquopyPythonRunner
import tj.pythontaj.execution.PythonRunner
import java.time.LocalDate

sealed interface ContentState {
    data object Loading : ContentState
    data class Ready(val curriculum: Curriculum) : ContentState
    data class Error(val message: String) : ContentState
}

data class ConsoleState(val running: Boolean = false, val result: RunResult? = null, val help: ErrorHelp? = null)

data class ExerciseUiState(val console: ConsoleState = ConsoleState(), val verdict: Verdict? = null)

/** Ҳолати умумии барнома. Мантиқи тоза дар domain/ ва content/ ҷойгир аст. */
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = LearningRepository(AppDatabase.get(app))
    private val runner: PythonRunner = ChaquopyPythonRunner(app)

    private val _content = MutableStateFlow<ContentState>(ContentState.Loading)
    val content: StateFlow<ContentState> = _content.asStateFlow()

    val completedLessons: StateFlow<Set<String>> = repo.completedLessonIds().stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())
    val passedExercises: StateFlow<Set<String>> = repo.passedExerciseIds().stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())
    val bookmarks: StateFlow<Set<String>> = repo.bookmarkIds().stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())
    val snippets: StateFlow<List<SnippetEntity>> = repo.snippets().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val streak: StateFlow<Int> = repo.studyDays()
        .map { StreakCalculator.currentStreak(it, today()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _console = MutableStateFlow(ConsoleState())
    val console: StateFlow<ConsoleState> = _console.asStateFlow()
    private val _exercise = MutableStateFlow(ExerciseUiState())
    val exercise: StateFlow<ExerciseUiState> = _exercise.asStateFlow()
    val editorCode = MutableStateFlow("print(\"Салом, ҷаҳон!\")\n")

    init {
        loadContent()
    }

    fun loadContent() {
        _content.value = ContentState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            _content.value = try {
                val assets = getApplication<Application>().assets
                val curriculum = ContentLoader { path -> assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() } }.load()
                ContentState.Ready(curriculum)
            } catch (e: Exception) {
                ContentState.Error(e.message ?: "Хатои номаълум ҳангоми хондани дарсҳо")
            }
        }
    }

    private fun today() = LocalDate.now().toEpochDay()

    fun attempts(exerciseId: String) = repo.attemptsFor(exerciseId)

    // ----- Муҳаррир -----
    fun runEditor() {
        if (_console.value.running) return
        val code = editorCode.value
        viewModelScope.launch {
            _console.value = ConsoleState(running = true)
            val r = runner.run(code)
            _console.value = ConsoleState(false, r, ErrorHelper.explain(r.errorType))
        }
    }

    fun stop() = runner.requestStop()
    fun clearConsole() { _console.value = ConsoleState() }
    fun saveSnippet() {
        val code = editorCode.value
        if (code.isBlank()) return
        val title = code.lineSequence().firstOrNull { it.isNotBlank() }?.take(40) ?: "Код"
        viewModelScope.launch { repo.saveSnippet(title, code) }
    }
    fun deleteSnippet(id: Long) { viewModelScope.launch { repo.deleteSnippet(id) } }

    // ----- Машқ -----
    fun resetExercise() { _exercise.value = ExerciseUiState() }

    fun runExercise(code: String) {
        if (_exercise.value.console.running) return
        viewModelScope.launch {
            _exercise.value = ExerciseUiState(ConsoleState(running = true))
            val r = runner.run(code)
            _exercise.value = ExerciseUiState(ConsoleState(false, r, ErrorHelper.explain(r.errorType)))
        }
    }

    fun stopExercise() = runner.requestStop()

    fun submitExercise(ex: Exercise, lessonId: String, code: String) {
        if (_exercise.value.console.running) return
        viewModelScope.launch {
            _exercise.value = ExerciseUiState(ConsoleState(running = true))
            val r = runner.run(ExerciseEvaluator.buildProgram(ex, code))
            val verdict = ExerciseEvaluator.evaluate(ex, code, r)
            val passed = verdict.kind == VerdictKind.CORRECT
            repo.recordAttempt(ex.id, lessonId, passed, code, today())
            if (passed) completeLessonIfDone(lessonId)
            // Дар функсияи FUNCTION баромади сунъӣ (@@RES@@) ба корбар нишон дода намешавад.
            val shown = if (ex.kind == ExerciseKind.FUNCTION) r.copy(stdout = r.stdout.lines().filter { !it.startsWith(ExerciseEvaluator.MARKER) }.joinToString("\n")) else r
            _exercise.value = ExerciseUiState(ConsoleState(false, shown, ErrorHelper.explain(r.errorType)), verdict)
        }
    }

    private suspend fun completeLessonIfDone(lessonId: String) {
        val cur = (_content.value as? ContentState.Ready)?.curriculum ?: return
        val lesson = cur.lesson(lessonId) ?: return
        val passed = repo.passedExerciseIdsOnce()
        if (lesson.exercises.all { it.id in passed }) repo.markLessonCompleted(lessonId, today())
    }

    /** Барои дарсҳои бе машқ. */
    fun completeLesson(lessonId: String) { viewModelScope.launch { repo.markLessonCompleted(lessonId, today()) } }

    // ----- Дигар -----
    fun toggleBookmark(lessonId: String) {
        val on = lessonId !in bookmarks.value
        viewModelScope.launch { repo.setBookmark(lessonId, on) }
    }

    fun recordQuiz(lessonId: String, score: Int, total: Int) {
        viewModelScope.launch { repo.recordQuiz(lessonId, score, total, today()) }
    }

    fun resetProgress() { viewModelScope.launch { repo.clearAll() } }
}
