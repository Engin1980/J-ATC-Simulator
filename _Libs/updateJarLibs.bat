ECHO OFF
ECHO Copying JAR libraries
SETLOCAL

ECHO Library eSystem
SET PF=eng.eSystem.jar
SET TCPF=C:\Users\Marek Vajgl\Documents\IdeaProjects\eng.eSystem\ESystem\out\artifacts\eng_eSystem\%PF%
IF EXIST "%TCPF%" (
    ECHO    Library found, updating
    COPY "%TCPF%" "%PF%" /Y
) ELSE (
    ECHO    Library not found
)

ECHO Library eSystem.events
SET PF=eng.eSystem.events.jar
SET TCPF=C:\Users\Marek Vajgl\Documents\IdeaProjects\eng.eSystem\events\out\artifacts\eng_eSystem_events\%PF%
IF EXIST "%TCPF%" (
    ECHO    Library found, updating
    COPY "%TCPF%" "%PF%" /Y
) ELSE (
    ECHO    Library not found
)

ECHO Library xmlSerialization
SET PF=EXmlSerialization.jar
SET TCPF=C:\Users\Marek Vajgl\Documents\IdeaProjects\eng.eSystem\xmlSerialization\out\artifacts\EXmlSerialization_jar\%PF%
IF EXIST "%TCPF%" (
    ECHO    Library found, updating
    COPY "%TCPF%" "%PF%" /Y
) ELSE (
    ECHO    Library not found
)

ENDLOCAL


