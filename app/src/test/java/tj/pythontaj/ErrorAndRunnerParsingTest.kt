package tj.pythontaj

import org.junit.Assert.*
import org.junit.Test
import tj.pythontaj.content.ErrorHelper
import tj.pythontaj.domain.RunStatus
import tj.pythontaj.execution.RunResultParser

class ErrorAndRunnerParsingTest {
    @Test fun allRequiredErrorTypesHaveHelp() {
        listOf(
            "SyntaxError", "IndentationError", "NameError", "TypeError", "ValueError", "IndexError",
            "KeyError", "ZeroDivisionError", "AttributeError", "ImportError", "ModuleNotFoundError",
        ).forEach {
            val h = ErrorHelper.explain(it)
            assertNotNull(it, h)
            assertTrue(it, h!!.causes.isNotEmpty() && h.goodExample.isNotBlank())
        }
    }

    @Test fun unknownErrorStillGetsGenericHelpAndNullGivesNothing() {
        assertNotNull(ErrorHelper.explain("WeirdError"))
        assertNull(ErrorHelper.explain(null))
    }

    @Test fun parsesRunnerJson() {
        val ok = RunResultParser.parse("""{"status":"OK","stdout":"hi\n","stderr":"","errorType":null,"errorMessage":null,"errorLine":null}""")
        assertEquals(RunStatus.OK, ok.status)
        assertEquals("hi\n", ok.stdout)
        assertNull(ok.errorType)
        val err = RunResultParser.parse("""{"status":"RUNTIME_ERROR","stdout":"","stderr":"x","errorType":"NameError","errorMessage":"m","errorLine":3}""")
        assertEquals(RunStatus.RUNTIME_ERROR, err.status)
        assertEquals(3, err.errorLine)
        assertEquals("NameError", err.errorType)
    }

    @Test fun unknownStatusBecomesEngineError() {
        assertEquals(RunStatus.ENGINE_ERROR, RunResultParser.parse("""{"status":"???"}""").status)
    }
}
