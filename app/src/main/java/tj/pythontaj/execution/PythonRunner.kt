package tj.pythontaj.execution

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import tj.pythontaj.domain.RunResult
import tj.pythontaj.domain.RunStatus
import java.io.File

interface PythonRunner {
    suspend fun run(code: String, stdin: String = "", timeoutMs: Long = DEFAULT_TIMEOUT_MS): RunResult
    fun requestStop()

    companion object {
        const val DEFAULT_TIMEOUT_MS = 5_000L
        const val MAX_OUTPUT_CHARS = 20_000
    }
}

/** Ҷудо кардани JSON-и runner.py ба RunResult. Тоза Kotlin, санҷидашаванда. */
object RunResultParser {
    fun parse(json: String): RunResult {
        val o = JSONObject(json)
        fun str(k: String): String? = if (o.isNull(k)) null else o.getString(k)
        val status = try {
            RunStatus.valueOf(o.getString("status"))
        } catch (e: IllegalArgumentException) {
            RunStatus.ENGINE_ERROR
        }
        return RunResult(
            status, o.optString("stdout", ""), o.optString("stderr", ""),
            str("errorType"), str("errorMessage"), if (o.isNull("errorLine")) null else o.getInt("errorLine"),
        )
    }
}

/** Иҷрои воқеии Python тавассути Chaquopy. Скрипт: app/src/main/python/runner.py */
class ChaquopyPythonRunner(private val context: Context) : PythonRunner {
    private val mutex = Mutex() // Ҳамзамон танҳо як иҷро: runner.py ҳолати умумӣ дорад.

    override suspend fun run(code: String, stdin: String, timeoutMs: Long): RunResult =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                try {
                    if (!Python.isStarted()) Python.start(AndroidPlatform(context))
                    val workDir = File(context.cacheDir, "learner").apply { mkdirs() }
                    val json = Python.getInstance().getModule("runner").callAttr(
                        "run_code", code, stdin, timeoutMs / 1000.0, PythonRunner.MAX_OUTPUT_CHARS, workDir.absolutePath,
                    ).toString()
                    RunResultParser.parse(json)
                } catch (e: Exception) {
                    RunResult(RunStatus.ENGINE_ERROR, stderr = e.message ?: e.javaClass.simpleName)
                }
            }
        }

    override fun requestStop() {
        try {
            if (Python.isStarted()) Python.getInstance().getModule("runner").callAttr("request_stop")
        } catch (_: Exception) {
        }
    }
}
