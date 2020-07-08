@ECHO OFF
ECHO Copying JAR libraries
SETLOCAL

ECHO Library eSystem
SET PF=eng.eSystem.jar
SET TCPF=..\..\..\_eSystem\%PF%
IF EXIST "%TCPF%" (
    ECHO    Library found, updating
    COPY "%TCPF%" "%PF%" /Y
) ELSE (
    ECHO    Library not found
)

ECHO Library xmlSerialization
SET PF=eng.eXmlSerialization.jar
SET TCPF=..\..\..\_eSystem\%PF%
IF EXIST "%TCPF%" (
    ECHO    Library found, updating
    COPY "%TCPF%" "%PF%" /Y
) ELSE (
    ECHO    Library not found
)

ENDLOCAL


