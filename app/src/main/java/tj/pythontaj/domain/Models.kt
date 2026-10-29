package tj.pythontaj.domain

/** Модели таълимӣ. Тавсифи формати JSON: docs/CONTENT_AUTHORING.md */
data class CourseModule(val id: String, val order: Int, val title: String, val description: String)

data class Section(val heading: String, val body: String)

data class CodeExample(
    val title: String,
    val code: String,
    val explanation: String,
    val expectedOutput: String,
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String,
)

enum class ExerciseKind { OUTPUT, FUNCTION }

/** Санҷиши функсия: [call] ифодаи Python, [expected] repr() и натиҷаи интизорӣ. */
data class FunctionTest(val call: String, val expected: String)

data class Exercise(
    val id: String,
    val title: String,
    val prompt: String,
    val difficulty: Int,
    val kind: ExerciseKind,
    val starterCode: String,
    val hints: List<String>,
    val expectedOutput: String,
    val tests: List<FunctionTest>,
    val requiredSnippets: List<String>,
    val solution: String,
    val explanation: String,
)

data class Lesson(
    val id: String,
    val moduleId: String,
    val title: String,
    val difficulty: Int,
    val objectives: List<String>,
    val prerequisites: List<String>,
    val introduction: String,
    val sections: List<Section>,
    val examples: List<CodeExample>,
    val commonMistakes: List<String>,
    val exercises: List<Exercise>,
    val quiz: List<QuizQuestion>,
    val summary: String,
    val completionCriteria: String,
)

enum class RunStatus { OK, SYNTAX_ERROR, RUNTIME_ERROR, TIMEOUT, STOPPED, OUTPUT_LIMIT, UNSUPPORTED, ENGINE_ERROR }

data class RunResult(
    val status: RunStatus,
    val stdout: String = "",
    val stderr: String = "",
    val errorType: String? = null,
    val errorMessage: String? = null,
    val errorLine: Int? = null,
)

enum class VerdictKind { CORRECT, INCORRECT, SYNTAX_ERROR, RUNTIME_ERROR, TIMEOUT, UNSUPPORTED }

data class Verdict(val kind: VerdictKind, val message: String)

data class ErrorHelp(
    val title: String,
    val explanation: String,
    val causes: List<String>,
    val badExample: String,
    val goodExample: String,
)
