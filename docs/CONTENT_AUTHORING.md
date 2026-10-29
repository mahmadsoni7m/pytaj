# Иловаи дарс ва машқ

Мазмуни курс дар `app/src/main/assets/curriculum/` аст. Барои иловаи дарс ба код даст задан лозим нест.

## Сохтор
- `modules.json` — рӯйхати модулҳо (id, order, title, description).
- `manifest.json` — тартиби дарсҳо (номи файлҳо). Тартиби ин рӯйхат тартиби омӯзиш аст.
- `lessons/<id>.json` — як дарс.

## Иловаи як дарс (қадамҳо)
1. Файли нав созед: `lessons/m02_l01.json` (нусха кардани `m01_l01.json` ва таҳрир).
2. Майдонҳои ҳатмӣ: `id, moduleId, title, difficulty (1..5), objectives, prerequisites, introduction, sections, examples, commonMistakes, exercises, quiz, summary, completionCriteria`.
3. `moduleId` бояд дар `modules.json` бошад; `prerequisites` — id-и дарсҳои **пештара** дар `manifest.json`.
4. Номи файлро ба охири `manifest.json` илова кунед: `"m02_l01.json"`.
5. Иҷро кунед: `python tools/validate_content.py`.

## Мисол: мисоли код
```json
{"title":"Ном","code":"print(2 + 3)","explanation":"Тавзеҳ","expectedOutput":"5"}
```
Validator ин кодро воқеан иҷро мекунад ва натиҷаро бо `expectedOutput` муқоиса мекунад.

## Иловаи як машқ
Дар массиви `exercises`-и дарс:
```json
{
  "id": "m02_l01_e1",
  "title": "Сарлавҳа",
  "prompt": "Шарти машқ",
  "difficulty": 1,
  "kind": "output",
  "starterCode": "",
  "hints": ["Маслиҳати 1"],
  "expectedOutput": "5",
  "requiredSnippets": ["print"],
  "solution": "print(2 + 3)",
  "explanation": "Чаро ин ҷавоб дуруст аст"
}
```
- `kind: "output"` — натиҷаи чопшуда бояд ба `expectedOutput` баробар бошад (фосилаҳои охири сатр нодида гирифта мешаванд).
- `kind: "function"` — ба ҷои `expectedOutput`, `tests` диҳед: `"tests":[{"call":"add(1, 2)","expected":"3"}]`. `expected` — натиҷаи `repr()`.
- `requiredSnippets` — порчаҳое, ки бояд дар коди корбар бошанд (масалан `sep`).
- `solution` ҳатман аз баҳодиҳӣ мегузарад (validator месанҷад) ва `starterCode` набояд аллакай дуруст бошад.

## Quiz
```json
{"id":"m02_l01_q1","question":"?","options":["А","Б","В"],"answerIndex":1,"explanation":"Чаро"}
```
ID-ҳои дарс, машқ ва савол дар тамоми курс беҳамто бошанд.

## Қоидаҳо
Калимаҳои калидии Python, код ва номҳои китобхонаи стандартиро тарҷума накунед.
