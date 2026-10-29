# PythonTAJ

Барномаи Android барои омӯзиши забони барномасозии **Python ба забони тоҷикӣ**. Дарсҳо ва иҷрои код бе интернет кор мекунанд.

> **Вазъи ҳозира: нусхаи 0.1.0 — асоси кории лоиҳа (на маҳсули пурра).**
> Сохтор, муҳаррики Python, баҳодиҳии машқҳо, захираи пешрафт ва 6 дарси аввал омодаанд.
> Курси пурра (100+ дарс) ҳанӯз навишта нашудааст. Рӯйхати дақиқ: [docs/STATUS.md](docs/STATUS.md).
> Компилятсияи Kotlin ва сохтани APK дар муҳити сохтан санҷида **нашудааст** — ин қадами аввали шумост (GitHub Actions).

## Имкониятҳои ҳозира
- Саҳифаи асосӣ: сатҳ, фоизи пешрафт, дарсҳои анҷомшуда, силсилаи рӯзҳо, тугмаи «Идома додан».
- 18 модули курс; Модули 1 («Муқаддимаи барномасозӣ») 6 дарс, 12 машқ ва 12 саволи quiz дорад.
- Хондани дарс: мақсадҳо, шарҳ, мисолҳо бо натиҷаи интизорӣ, хатоҳои маъмул, хулоса.
- Муҳаррири код + Run/Stop + консол; захира ва кушодани порчаҳои код.
- Машқҳо бо баҳодиҳии худкор, маслиҳат, таърихи кӯшишҳо ва ҳалли намунавӣ.
- Ёрии хатоҳо ба тоҷикӣ (SyntaxError, IndentationError, NameError, TypeError, ValueError, IndexError, KeyError, ZeroDivisionError, AttributeError, ImportError).
- Ҷустуҷӯ, захираи дарсҳо, пок кардани пешрафт бо тасдиқ. Ҳамаи маълумот дар Room (SQLite) маҳаллӣ аст.

## Сохтори лоиҳа
```
app/src/main/java/tj/pythontaj/
  domain/      моделҳо ва ҳисоби пешрафт/силсила
  content/     хондани JSON, санҷиши курс, баҳодиҳии машқҳо, ёрии хатоҳо
  execution/   иҷрои Python (Chaquopy)
  data/        Room: ҷадвалҳо ва repository
  ui/          экранҳои Compose, мавзӯъ, матнҳои тоҷикӣ
app/src/main/python/runner.py          иҷрогари Python (вақт, ҳаҷми натиҷа, import-ҳои манъшуда)
app/src/main/assets/curriculum/        дарсҳо дар JSON
tools/validate_content.py              санҷиши курс (мисолҳоро воқеан иҷро мекунад)
.github/workflows/android.yml          сохтани APK дар GitHub
docs/                                  ҳуҷҷатҳои муфассал
```

## Технологияҳо
Kotlin 1.9.24, Jetpack Compose (BOM 2024.06.00), Material 3, Navigation Compose, Room 2.6.1, Coroutines/Flow, Chaquopy 15.0.1 (Python 3.11), AGP 8.5.2, Gradle 8.7, JDK 17. minSdk 26 (Android 8.0).

## Чӣ тавр APK гирифтан
Пурра: [docs/BUILD_APK.md](docs/BUILD_APK.md). Мухтасар:
1. Лоиҳаро ба GitHub бор кунед (branch `main`).
2. Дар GitHub: **Actions → Android CI → run → Artifacts → PythonTAJ-Android-APK**.
3. Файли `PythonTAJ.apk`-ро ба телефон интиқол диҳед ва насб кунед.

## Иловаи дарс ва машқ
[docs/CONTENT_AUTHORING.md](docs/CONTENT_AUTHORING.md). Пас аз тағйир: `python tools/validate_content.py`.

## Санҷишҳо
- `python tools/validate_content.py` — мазмуни курс (дар муҳити сохтан иҷро ва гузашт).
- `gradle :app:testDebugUnitTest` — unit-тестҳои Kotlin (навишта шудаанд, ҳанӯз иҷро **нашудаанд**).

## Маҳдудиятҳои маълум
Ниг. [docs/STATUS.md](docs/STATUS.md) ва [docs/PYTHON_RUNTIME.md](docs/PYTHON_RUNTIME.md). Муҳимтаринаш: иҷрогар sandbox-и пурраи амн нест.

## Иҷозатнома
MIT, ниг. LICENSE.
