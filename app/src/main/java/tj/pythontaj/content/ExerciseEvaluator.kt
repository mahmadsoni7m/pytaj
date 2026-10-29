package tj.pythontaj.content

import tj.pythontaj.domain.*

/** Баҳодиҳии машқҳо. Тоза Kotlin аст, бинобар ин бо unit-тест санҷида мешавад. */
object ExerciseEvaluator {
    const val MARKER = "@@RES@@ "

    /** Барои машқи FUNCTION ба коди корбар даъвати санҷишӣ илова мешавад. */
    fun buildProgram(ex: Exercise, userCode: String): String {
        if (ex.kind != ExerciseKind.FUNCTION) return userCode
        return buildString {
            append(userCode.trimEnd()).append("\n")
            ex.tests.forEach { append("print('").append(MARKER).append("' + repr(").append(it.call).append("))\n") }
        }
    }

    fun evaluate(ex: Exercise, userCode: String, r: RunResult): Verdict {
        when (r.status) {
            RunStatus.SYNTAX_ERROR -> return Verdict(VerdictKind.SYNTAX_ERROR, "Хатои синтаксис дар сатри ${r.errorLine ?: "?"}: ${r.errorMessage.orEmpty()}")
            RunStatus.RUNTIME_ERROR -> return Verdict(VerdictKind.RUNTIME_ERROR, "Хатои иҷро (${r.errorType}): ${r.errorMessage.orEmpty()}")
            RunStatus.TIMEOUT, RunStatus.STOPPED -> return Verdict(VerdictKind.TIMEOUT, "Барнома хеле дароз кор кард ё қатъ шуд. Оё даври беохир нест?")
            RunStatus.OUTPUT_LIMIT -> return Verdict(VerdictKind.INCORRECT, "Баромад аз ҳад зиёд аст. Оё даври беохири print нест?")
            RunStatus.UNSUPPORTED, RunStatus.ENGINE_ERROR -> return Verdict(VerdictKind.UNSUPPORTED, "Муҳаррики Python ин амалро иҷро карда натавонист: ${r.stderr}")
            RunStatus.OK -> Unit
        }
        val missing = ex.requiredSnippets.filter { !userCode.contains(it) }
        if (missing.isNotEmpty()) {
            return Verdict(VerdictKind.INCORRECT, "Дар код бояд ин қисмҳо бошанд: ${missing.joinToString(", ")}")
        }
        return when (ex.kind) {
            ExerciseKind.OUTPUT -> {
                if (normalize(r.stdout) == normalize(ex.expectedOutput)) Verdict(VerdictKind.CORRECT, "Офарин! Натиҷа дуруст аст.")
                else Verdict(VerdictKind.INCORRECT, "Натиҷа мувофиқ нест.\nИнтизор:\n${normalize(ex.expectedOutput)}\nШумо гирифтед:\n${normalize(r.stdout)}")
            }
            ExerciseKind.FUNCTION -> {
                val got = r.stdout.lines().filter { it.startsWith(MARKER) }.map { it.removePrefix(MARKER).trimEnd() }
                if (got.size != ex.tests.size) return Verdict(VerdictKind.INCORRECT, "Санҷишҳо пурра иҷро нашуданд.")
                val bad = ex.tests.indices.firstOrNull { got[it] != ex.tests[it].expected.trim() }
                if (bad == null) Verdict(VerdictKind.CORRECT, "Офарин! Ҳамаи санҷишҳо гузаштанд.")
                else Verdict(VerdictKind.INCORRECT, "Санҷиш нагузашт: ${ex.tests[bad].call}\nИнтизор: ${ex.tests[bad].expected}\nГирифтед: ${got[bad]}")
            }
        }
    }

    fun normalize(s: String): String =
        s.replace("\r\n", "\n").lines().joinToString("\n") { it.trimEnd() }.trimEnd()
}
