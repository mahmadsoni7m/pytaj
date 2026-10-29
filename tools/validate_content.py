#!/usr/bin/env python3
"""Санҷиши мазмуни курс (дарсҳо, машқҳо, quiz).

Иҷро:  python tools/validate_content.py

Санҷишҳо:
  * сохтори JSON ва майдонҳои ҳатмӣ;
  * ID-ҳои такрорӣ, истинодҳои вайроншудаи модул ва пешшарт, тартиби пешшартҳо;
  * ҳар мисоли код ВОҚЕАН бо app/src/main/python/runner.py иҷро мешавад ва натиҷааш
    бо expectedOutput муқоиса мешавад;
  * ҳалли ҳар машқ (solution) бо ҳамон қоидаи баҳодиҳӣ санҷида мешавад;
  * starterCode-и машқ набояд аллакай ҳал бошад.
Хатоҳо -> exit code 1.
"""
import json
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "app/src/main/assets/curriculum"
sys.path.insert(0, str(ROOT / "app/src/main/python"))
import runner  # noqa: E402

MARKER = "@@RES@@ "
TARGETS = {"lessons": 100, "exercises": 150, "quiz": 100, "projects": 10}
errors, warnings = [], []


def err(msg):
    errors.append(msg)


def norm(s):
    return "\n".join(l.rstrip() for l in s.replace("\r\n", "\n").split("\n")).rstrip()


def run(code, workdir):
    return json.loads(runner.run_code(code, "", 5.0, 20000, workdir))


def build_program(ex, code):
    if ex["kind"].lower() != "function":
        return code
    lines = [code.rstrip()]
    for t in ex.get("tests", []):
        lines.append("print('%s' + repr(%s))" % (MARKER, t["call"]))
    return "\n".join(lines) + "\n"


def evaluate(ex, code, workdir):
    r = run(build_program(ex, code), workdir)
    if r["status"] != "OK":
        return False, "%s %s" % (r["status"], r.get("errorMessage"))
    missing = [s for s in ex.get("requiredSnippets", []) if s not in code]
    if missing:
        return False, "requiredSnippets: %s" % missing
    if ex["kind"].lower() == "output":
        ok = norm(r["stdout"]) == norm(ex["expectedOutput"])
        return ok, "got %r" % r["stdout"]
    got = [l[len(MARKER):].rstrip() for l in r["stdout"].split("\n") if l.startswith(MARKER)]
    exp = [t["expected"].strip() for t in ex["tests"]]
    return got == exp, "got %r" % got


def main():
    try:
        modules = json.loads((ASSETS / "modules.json").read_text(encoding="utf-8"))["modules"]
        manifest = json.loads((ASSETS / "manifest.json").read_text(encoding="utf-8"))["lessons"]
    except Exception as e:  # noqa: BLE001
        print("FATAL:", e)
        return 1

    module_ids = [m["id"] for m in modules]
    if len(set(module_ids)) != len(module_ids):
        err("ID-и модулҳо такрор шудаанд")

    lessons = []
    for fn in manifest:
        p = ASSETS / "lessons" / fn
        if not p.exists():
            err("%s: файл нест" % fn)
            continue
        try:
            lessons.append(json.loads(p.read_text(encoding="utf-8")))
        except json.JSONDecodeError as e:
            err("%s: JSON хато: %s" % (fn, e))
    on_disk = {p.name for p in (ASSETS / "lessons").glob("*.json")}
    for extra in sorted(on_disk - set(manifest)):
        err("%s: дар manifest.json нест" % extra)

    ids = [l["id"] for l in lessons]
    if len(set(ids)) != len(ids):
        err("ID-и дарсҳо такрор шудаанд")
    pos = {i: n for n, i in enumerate(ids)}

    required = ["id", "moduleId", "title", "difficulty", "objectives", "prerequisites", "introduction", "sections",
                "examples", "commonMistakes", "exercises", "quiz", "summary", "completionCriteria"]
    ex_ids, quiz_ids = [], []
    n_ex = n_q = 0
    with tempfile.TemporaryDirectory() as wd:
        for l in lessons:
            lid = l.get("id", "?")
            for k in required:
                if k not in l or l[k] in ("", None):
                    if k not in ("prerequisites",):
                        err("%s: майдони '%s' намерасад ё холӣ аст" % (lid, k))
            if l.get("moduleId") not in module_ids:
                err("%s: модули %s вуҷуд надорад" % (lid, l.get("moduleId")))
            for p in l.get("prerequisites", []):
                if p not in pos:
                    err("%s: пешшарти %s вуҷуд надорад" % (lid, p))
                elif pos[p] >= pos[lid]:
                    err("%s: пешшарти %s бояд пештар ояд" % (lid, p))
            for e in l.get("examples", []):
                r = run(e["code"], wd)
                if r["status"] != "OK":
                    err("%s: мисоли '%s' иҷро нашуд: %s %s" % (lid, e["title"], r["status"], r.get("errorMessage")))
                elif norm(r["stdout"]) != norm(e["expectedOutput"]):
                    err("%s: мисоли '%s' натиҷаи нодуруст: %r != %r" % (lid, e["title"], r["stdout"], e["expectedOutput"]))
            for x in l.get("exercises", []):
                n_ex += 1
                ex_ids.append(x["id"])
                kind = x.get("kind", "").lower()
                if kind not in ("output", "function"):
                    err("%s: kind нодуруст" % x["id"])
                    continue
                if kind == "output" and not x.get("expectedOutput", "").strip():
                    err("%s: expectedOutput холӣ аст" % x["id"])
                if kind == "function" and not x.get("tests"):
                    err("%s: tests холӣ аст" % x["id"])
                ok, info = evaluate(x, x["solution"], wd)
                if not ok:
                    err("%s: solution аз баҳодиҳӣ намегузарад (%s)" % (x["id"], info))
                if x.get("starterCode", "").strip():
                    ok2, _ = evaluate(x, x["starterCode"], wd)
                    if ok2:
                        err("%s: starterCode аллакай ҳал шудааст" % x["id"])
            for qq in l.get("quiz", []):
                n_q += 1
                quiz_ids.append(qq["id"])
                if len(qq["options"]) < 2 or not (0 <= qq["answerIndex"] < len(qq["options"])):
                    err("%s: варианти ҷавоб нодуруст" % qq["id"])
    for name, lst in (("машқ", ex_ids), ("quiz", quiz_ids)):
        if len(set(lst)) != len(lst):
            err("ID-и %s такрор шудаанд" % name)

    empty = [m["id"] for m in modules if not any(l["moduleId"] == m["id"] for l in lessons)]
    print("Дарсҳо: %d/%d | Машқҳо: %d/%d | Саволҳои quiz: %d/%d | Лоиҳаҳо: 0/%d" % (
        len(lessons), TARGETS["lessons"], n_ex, TARGETS["exercises"], n_q, TARGETS["quiz"], TARGETS["projects"]))
    if empty:
        print("Огоҳӣ: модулҳои бе дарс (ҳанӯз навишта нашудаанд): %s" % ", ".join(empty))
    if errors:
        print("\nХАТОҲО (%d):" % len(errors))
        for e in errors:
            print(" -", e)
        return 1
    print("Мазмун дуруст аст (ҳамаи мисолҳо иҷро ва ҳалҳо санҷида шуданд).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
