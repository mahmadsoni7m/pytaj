# Сохтани APK

## Муҳим дар бораи Gradle wrapper
Дар ин лоиҳа файлҳои `gradlew`, `gradlew.bat` ва `gradle/wrapper/gradle-wrapper.jar` **нестанд**: JAR-и воқеиро бе интернет гирифта натавонистам ва JAR-и сохта намесозам.
- `gradle/wrapper/gradle-wrapper.properties` (Gradle 8.7) мавҷуд аст.
- GitHub Actions худаш Gradle 8.7-ро насб мекунад (`gradle/actions/setup-gradle`), бинобар ин барои сохтани APK дар GitHub wrapper лозим нест.
- Барои корношоямӣ дар компютери худ: Android Studio лоиҳаро кушода, wrapper-ро худаш месозад, ё: `gradle wrapper --gradle-version 8.7` (Gradle бояд насб бошад). Баъд `gradlew`, `gradlew.bat` ва `gradle/wrapper/gradle-wrapper.jar`-ро ба Git илова кунед.

## 1. Бор кардан ба GitHub
1. Дар https://github.com ҳисоб созед ва **New repository** -ро пахш кунед (масалан `PythonTAJ`, бе README).
2. ZIP-ро кушоед. Дар папкаи `PythonTAJ` терминал кушоед:
```
git init
git add .
git commit -m "PythonTAJ 0.1.0"
git branch -M main
git remote add origin https://github.com/<НОМИ_ШУМО>/PythonTAJ.git
git push -u origin main
```
(Бор кардани папкаи пинҳонии `.github` тавассути браузер баъзан кор намекунад; `git` ё GitHub Desktop боэътимодтар аст.)

## 2. Гирифтани APK
1. Дар репозиторий: **Actions** → ҷараёни **Android CI** → иҷрои охирин.
2. Агар сабз бошад: поёни саҳифа, **Artifacts** → `PythonTAJ-Android-APK` -ро зеркашӣ кунед (ZIP, дар дохилаш `PythonTAJ.apk`).
3. Агар сурх бошад: лавҳаи хатоҳоро кушоед. Дар **нусхаи аввал хатоҳои компилятсия эҳтимол доранд** (ман Kotlin-ро дар муҳити сохтан иҷро карда натавонистам). Матни хаторо ба ман фиристед, то ислоҳ кунам.

## 3. Насб дар телефон
Файли APK-ро ба телефон гузаронед, онро кушоед ва иҷозати «насб аз сарчашмаи номаълум»-ро диҳед.

## Фарқи навъҳо
- **Debug APK** — барои санҷиш; бо калиди санҷишӣ имзо шудааст. Ин чизест, ки workflow месозад.
- **Release APK** — имзошуда бо калиди шахсии шумо; барои паҳн кардан берун аз Google Play.
- **AAB** — формати Google Play.

Барои release калиди шахсӣ созед, онро **ҳеҷ гоҳ ба Git набаред**, ва танҳо тавассути GitHub Secrets (Settings → Secrets and variables → Actions) ба workflow диҳед. Workflow-и release ҳанӯз навишта нашудааст.
