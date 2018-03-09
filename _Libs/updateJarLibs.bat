ECHO OFF
ECHO Copying JAR libraries
SETLOCAL

ECHO Library eSystem
SET PF=eng.eSystem.jar
SET TCPF=C:\Users\Marek Vajgl\Documents\IdeaProjects\_eSystem\eng.eSystem\out\artifacts\%PF%
IF EXIST "%TCPF%" (
    ECHO    Library found, updating
    COPY "%TCPF%" "%PF%" /Y
) ELSE (
    ECHO    Library not found
)

ECHO Library xmlSerialization
SET PF=EXmlSerialization.jar
SET TCPF=C:\Users\Marek Vajgl\Documents\IdeaProjects\_eSystem\eng.eSystem.xmlSerialization\out\artifacts\EXmlSerialization_jar\%PF%
IF EXIST "%TCPF%" (
    ECHO    Library found, updating
    COPY "%TCPF%" "%PF%" /Y
) ELSE (
    ECHO    Library not found
)

ENDLOCAL


