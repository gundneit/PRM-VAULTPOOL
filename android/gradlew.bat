@rem Simple wrapper delegating to original Gradle wrapper script content.
@rem Bạn có thể cũng copy file gradlew.bat gốc vào đây nếu cần chạy trên Windows.

@if "%DEBUG%"=="" @echo off
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%
set GRADLE_WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

set JAVA_EXE=java.exe

if exist "%JAVA_EXE%" goto execute

echo Java not found. Please ensure JAVA_HOME or PATH is set correctly.
goto fail

:execute
"%JAVA_EXE%" "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%GRADLE_WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
goto end

:fail
exit /b 1

:end

