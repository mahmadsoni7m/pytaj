"""Муҳаррики иҷрои коди Python барои PythonTAJ.

Ин файл ҳам дар Android (тавассути Chaquopy), ҳам дар компютер (tools/validate_content.py)
истифода мешавад, то натиҷаҳо якхела бошанд.

Муҳим: ин санҷиши ҳадди иҷро аст, на sandbox-и пурраи амн.
  * Вақт: трейсер дар ҳар сатри Python вақтро месанҷад. Амалҳои дарозмуддати C
    (масалан 10**10**10) ё `except BaseException: pass` дар даври беохир онро
    метавонанд дур зананд.
  * Import-ҳои шабакавӣ/раванд барои коди корбар манъ шудаанд, вале os ва ғайра боз мемонанд.
  * open() танҳо дар папкаи корӣ (workdir) кор мекунад.
"""
import builtins
import io
import json
import linecache
import os
import sys
import threading
import time
import traceback

_MAIN = "<main>"
_BLOCKED_IMPORTS = {
    "socket", "ssl", "subprocess", "ctypes", "multiprocessing", "http", "urllib",
    "ftplib", "smtplib", "telnetlib", "poplib", "imaplib", "xmlrpc", "webbrowser",
}
_stop_flag = threading.Event()


class ExecutionTimeout(BaseException):
    pass


class ExecutionStopped(BaseException):
    pass


class OutputLimitExceeded(BaseException):
    pass


class _Out:
    """Нависандаи маҳдудшуда: аз ҳад зиёд баромадро қатъ мекунад."""

    def __init__(self, limit):
        self.parts = []
        self.size = 0
        self.limit = limit

    def write(self, s):
        if not isinstance(s, str):
            s = str(s)
        before = self.size
        self.size += len(s)
        if self.size > self.limit:
            self.parts.append(s[: max(0, self.limit - before)])
            raise OutputLimitExceeded()
        self.parts.append(s)
        return len(s)

    def flush(self):
        pass

    def getvalue(self):
        return "".join(self.parts)


def request_stop():
    """Аз риштаи дигар даъват мешавад (тугмаи Stop)."""
    _stop_flag.set()


def engine_info():
    return sys.version.split()[0]


def _make_import(real_import):
    def guarded(name, globals=None, locals=None, fromlist=(), level=0):
        importer = (globals or {}).get("__name__")
        if level == 0 and importer == "__main__" and name.split(".")[0] in _BLOCKED_IMPORTS:
            raise ImportError("Модули '%s' дар PythonTAJ иҷозат дода намешавад" % name.split(".")[0])
        return real_import(name, globals, locals, fromlist, level)

    return guarded


def _make_open(real_open, workdir):
    base = os.path.realpath(workdir) if workdir else None

    def guarded(file, mode="r", *args, **kwargs):
        if base is None or isinstance(file, int):
            raise PermissionError("Кор бо файл дар ин муҳит дастнорас аст")
        path = os.path.realpath(os.path.join(base, os.fspath(file)))
        if os.path.commonpath([base, path]) != base:
            raise PermissionError("Танҳо файлҳои дохили папкаи корӣ иҷозат дода мешаванд")
        return real_open(path, mode, *args, **kwargs)

    return guarded


def _result(status, stdout="", stderr="", etype=None, emsg=None, eline=None):
    return json.dumps(
        {
            "status": status,
            "stdout": stdout,
            "stderr": stderr,
            "errorType": etype,
            "errorMessage": emsg,
            "errorLine": eline,
        },
        ensure_ascii=False,
    )


def run_code(code, stdin_text="", timeout_seconds=5.0, max_output=20000, workdir=None):
    _stop_flag.clear()
    try:
        compiled = compile(code, _MAIN, "exec")
    except SyntaxError as e:  # IndentationError низ ҳамин ҷо меафтад
        text = "".join(traceback.format_exception_only(type(e), e))
        return _result("SYNTAX_ERROR", "", text, type(e).__name__, e.msg or str(e), e.lineno)
    except ValueError as e:
        return _result("SYNTAX_ERROR", "", "ValueError: %s\n" % e, "ValueError", str(e), None)

    out, err = _Out(max_output), _Out(max_output)
    stdin = io.StringIO(stdin_text)
    deadline = time.monotonic() + float(timeout_seconds)

    def tracer(frame, event, arg):
        if _stop_flag.is_set():
            raise ExecutionStopped()
        if time.monotonic() > deadline:
            raise ExecutionTimeout()
        return tracer

    def fake_input(prompt=""):
        out.write(str(prompt))
        line = stdin.readline()
        if line == "":
            raise EOFError("EOF when reading a line")
        return line.rstrip("\n")

    safe = dict(builtins.__dict__)
    safe["input"] = fake_input
    safe["__import__"] = _make_import(builtins.__import__)
    safe["open"] = _make_open(builtins.open, workdir)
    safe.pop("breakpoint", None)
    env = {"__name__": "__main__", "__builtins__": safe}

    linecache.cache[_MAIN] = (len(code), None, code.splitlines(True), _MAIN)
    old_out, old_err, old_trace = sys.stdout, sys.stderr, sys.gettrace()
    status, etype, emsg, eline, tb_text = "OK", None, None, None, ""
    sys.stdout, sys.stderr = out, err
    try:
        sys.settrace(tracer)
        exec(compiled, env)
    except ExecutionTimeout:
        status = "TIMEOUT"
    except ExecutionStopped:
        status = "STOPPED"
    except OutputLimitExceeded:
        status = "OUTPUT_LIMIT"
    except SystemExit:
        pass
    except BaseException as e:  # хатои корбар
        sys.settrace(None)
        status, etype, emsg = "RUNTIME_ERROR", type(e).__name__, str(e)
        tb = e.__traceback__
        while tb is not None and tb.tb_frame.f_code.co_filename != _MAIN:
            tb = tb.tb_next
        t = tb
        while t is not None:
            if t.tb_frame.f_code.co_filename == _MAIN:
                eline = t.tb_lineno
            t = t.tb_next
        tb_text = "".join(traceback.format_exception(type(e), e, tb))
    finally:
        sys.settrace(old_trace)
        sys.stdout, sys.stderr = old_out, old_err
        linecache.cache.pop(_MAIN, None)
    return _result(status, out.getvalue(), err.getvalue() + tb_text, etype, emsg, eline)
