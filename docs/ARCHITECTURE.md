# Меъмории PythonTAJ

```
UI (Compose)  ->  MainViewModel  ->  LearningRepository -> Room (SQLite)
                       |-> ContentLoader/Curriculum (JSON дар assets, танҳо хониш)
                       |-> ExerciseEvaluator (Kotlin тоза)
                       |-> PythonRunner -> Chaquopy -> runner.py
```
- **Мазмун ҷудо аз UI**: дарсҳо JSON ҳастанд, `ContentLoader` онҳоро мехонад ва `CurriculumValidator` ҳангоми боркунӣ месанҷад. Агар JSON вайрон бошад, корбар паёми хато ва тугмаи «Аз нав кӯшиш кунед»-ро мебинад (на садама).
- **Пешрафт** аз маълумоти воқеии Room ҳисоб мешавад. Дарс вақте анҷом мешавад, ки **ҳамаи машқҳояш** гузашта бошанд. Дар корбари нав ҳама чиз 0 аст.
- **Баҳодиҳӣ** (`ExerciseEvaluator`): санҷиши натиҷа (OUTPUT) ё санҷиши функсия бо `repr()` (FUNCTION) + порчаҳои ҳатмӣ. Ҳолатҳо ҷудо мешаванд: дуруст, нодуруст, SyntaxError, хатои иҷро, timeout, дастнорас.
- **Room**: версияи 1, `exportSchema=true`. Барои тағйири сохтор `version`-ро зиёд кунед ва `Migration` илова кунед; `fallbackToDestructiveMigration` ИСТИФОДА НАМЕШАВАД, то пешрафт нест нашавад.
- **Қарори матнҳо**: матнҳои UI дар `ui/Texts.kt` (барнома танҳо як забон дорад), на `strings.xml`.
- **DataStore** ҳоло истифода намешавад (нигаред STATUS.md).
