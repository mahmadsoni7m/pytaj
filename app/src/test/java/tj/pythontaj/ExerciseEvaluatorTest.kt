package tj.pythontaj

import org.junit.Assert.*
import org.junit.Test
import tj.pythontaj.content.ExerciseEvaluator
import tj.pythontaj.domain.*

class ExerciseEvaluatorTest {
    private fun ok(out: String) = RunResult(RunStatus.OK, stdout = out)

    @Test fun exactOutputIsCorrect() {
        val v = ExerciseEvaluator.evaluate(exercise(expected = "Салом"), "print('Салом')", ok("Салом\n"))
        assertEquals(VerdictKind.CORRECT, v.kind)
    }

    @Test fun trailingWhitespaceIsIgnored() {
        val v = ExerciseEvaluator.evaluate(exercise(expected = "a\nb"), "x", ok("a  \r\nb\n\n"))
        assertEquals(VerdictKind.CORRECT, v.kind)
    }

    @Test fun wrongOutputIsIncorrect() {
        val v = ExerciseEvaluator.evaluate(exercise(expected = "a"), "x", ok("b\n"))
        assertEquals(VerdictKind.INCORRECT, v.kind)
    }

    @Test fun missingRequiredSnippetIsIncorrect() {
        val v = ExerciseEvaluator.evaluate(exercise(expected = "a", required = listOf("sep")), "print('a')", ok("a\n"))
        assertEquals(VerdictKind.INCORRECT, v.kind)
        assertTrue(v.message.contains("sep"))
    }

    @Test fun errorStatusesMapToVerdicts() {
        val ex = exercise()
        assertEquals(VerdictKind.SYNTAX_ERROR, ExerciseEvaluator.evaluate(ex, "", RunResult(RunStatus.SYNTAX_ERROR, errorLine = 1)).kind)
        assertEquals(VerdictKind.RUNTIME_ERROR, ExerciseEvaluator.evaluate(ex, "", RunResult(RunStatus.RUNTIME_ERROR, errorType = "NameError")).kind)
        assertEquals(VerdictKind.TIMEOUT, ExerciseEvaluator.evaluate(ex, "", RunResult(RunStatus.TIMEOUT)).kind)
        assertEquals(VerdictKind.UNSUPPORTED, ExerciseEvaluator.evaluate(ex, "", RunResult(RunStatus.ENGINE_ERROR)).kind)
    }

    @Test fun functionExerciseBuildsHarnessAndChecksResults() {
        val ex = exercise(kind = ExerciseKind.FUNCTION, expected = "", tests = listOf(FunctionTest("add(1, 2)", "3"), FunctionTest("add(0, 0)", "0")))
        val program = ExerciseEvaluator.buildProgram(ex, "def add(a, b): return a + b")
        assertTrue(program.contains("repr(add(1, 2))"))
        val good = ok("noise\n${ExerciseEvaluator.MARKER}3\n${ExerciseEvaluator.MARKER}0\n")
        assertEquals(VerdictKind.CORRECT, ExerciseEvaluator.evaluate(ex, "x", good).kind)
        val bad = ok("${ExerciseEvaluator.MARKER}3\n${ExerciseEvaluator.MARKER}1\n")
        val v = ExerciseEvaluator.evaluate(ex, "x", bad)
        assertEquals(VerdictKind.INCORRECT, v.kind)
        assertTrue(v.message.contains("add(0, 0)"))
        assertEquals(VerdictKind.INCORRECT, ExerciseEvaluator.evaluate(ex, "x", ok("")).kind)
    }
}
