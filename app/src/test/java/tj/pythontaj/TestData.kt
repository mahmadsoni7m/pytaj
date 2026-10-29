package tj.pythontaj

import tj.pythontaj.domain.*

fun exercise(
    id: String = "e1", kind: ExerciseKind = ExerciseKind.OUTPUT, expected: String = "ok",
    tests: List<FunctionTest> = emptyList(), required: List<String> = emptyList(),
) = Exercise(id, "t", "p", 1, kind, "", emptyList(), expected, tests, required, "sol", "expl")

fun lesson(
    id: String, moduleId: String = "m01", prerequisites: List<String> = emptyList(),
    exercises: List<Exercise> = listOf(exercise("$id-e")),
    quiz: List<QuizQuestion> = listOf(QuizQuestion("$id-q", "q", listOf("a", "b"), 0, "x")),
) = Lesson(
    id, moduleId, "title $id", 1, listOf("o"), prerequisites, "intro", listOf(Section("h", "b")),
    listOf(CodeExample("t", "print(1)", "e", "1")), listOf("m"), exercises, quiz, "sum", "crit",
)
