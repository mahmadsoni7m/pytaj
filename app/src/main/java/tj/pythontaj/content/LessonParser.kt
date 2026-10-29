package tj.pythontaj.content

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import tj.pythontaj.domain.*

class ContentException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Хондани JSON ба моделҳо. Хатои формат ҳамеша ContentException мешавад. */
object LessonParser {
    fun parseModules(json: String): List<CourseModule> = guard("modules.json") {
        JSONObject(json).getJSONArray("modules").objects().map {
            CourseModule(it.getString("id"), it.getInt("order"), it.getString("title"), it.getString("description"))
        }
    }

    fun parseLesson(json: String, source: String = "lesson"): Lesson = guard(source) {
        val o = JSONObject(json)
        Lesson(
            id = o.getString("id"),
            moduleId = o.getString("moduleId"),
            title = o.getString("title"),
            difficulty = o.getInt("difficulty"),
            objectives = o.getJSONArray("objectives").strings(),
            prerequisites = o.getJSONArray("prerequisites").strings(),
            introduction = o.getString("introduction"),
            sections = o.getJSONArray("sections").objects().map { Section(it.getString("heading"), it.getString("body")) },
            examples = o.getJSONArray("examples").objects().map {
                CodeExample(it.getString("title"), it.getString("code"), it.getString("explanation"), it.getString("expectedOutput"))
            },
            commonMistakes = o.getJSONArray("commonMistakes").strings(),
            exercises = o.getJSONArray("exercises").objects().map { parseExercise(it) },
            quiz = o.getJSONArray("quiz").objects().map {
                QuizQuestion(
                    it.getString("id"), it.getString("question"), it.getJSONArray("options").strings(),
                    it.getInt("answerIndex"), it.getString("explanation"),
                )
            },
            summary = o.getString("summary"),
            completionCriteria = o.getString("completionCriteria"),
        )
    }

    private fun parseExercise(o: JSONObject) = Exercise(
        id = o.getString("id"),
        title = o.getString("title"),
        prompt = o.getString("prompt"),
        difficulty = o.getInt("difficulty"),
        kind = ExerciseKind.valueOf(o.getString("kind").uppercase()),
        starterCode = o.optString("starterCode", ""),
        hints = o.optJSONArray("hints").stringsOrEmpty(),
        expectedOutput = o.optString("expectedOutput", ""),
        tests = (o.optJSONArray("tests")?.objects() ?: emptyList()).map { FunctionTest(it.getString("call"), it.getString("expected")) },
        requiredSnippets = o.optJSONArray("requiredSnippets").stringsOrEmpty(),
        solution = o.getString("solution"),
        explanation = o.getString("explanation"),
    )

    private fun <T> guard(source: String, block: () -> T): T = try {
        block()
    } catch (e: JSONException) {
        throw ContentException("$source: ${e.message}", e)
    } catch (e: IllegalArgumentException) {
        throw ContentException("$source: арзиши нодуруст (${e.message})", e)
    }

    private fun JSONArray.strings(): List<String> = List(length()) { getString(it) }
    private fun JSONArray?.stringsOrEmpty(): List<String> = this?.strings() ?: emptyList()
    private fun JSONArray.objects(): List<JSONObject> = List(length()) { getJSONObject(it) }
}
