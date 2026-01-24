@rem ---------------------------------------------------------------------------
@rem Gradle startup script for Windows
@rem ---------------------------------------------------------------------------
@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
set JAVA_EXE=
if defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
)
if not defined JAVA_EXE (
  set JAVA_EXE=java.exe
)

@rem Verify Java exists
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% NEQ 0 (
  echo.
  echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
  echo.
  exit /b 1
)

set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar;%APP_HOME%gradle\wrapper\gradle-wrapper-shared.jar

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

endlocal
