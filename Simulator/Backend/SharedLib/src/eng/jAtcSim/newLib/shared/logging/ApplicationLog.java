package eng.jAtcSim.newLib.shared.logging;

import eng.eSystem.events.EventAnonymous;
import eng.eSystem.utilites.ExceptionUtils;

import java.io.IOException;
import java.util.Optional;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ApplicationLog {

  public static class AppLogMessage {
    public final String text;
    public final LogItemType type;

    private AppLogMessage(LogItemType type, String text) {
      this.text = text;
      this.type = type;
    }
  }

  private static final java.time.format.DateTimeFormatter DATE_TIME_FORMATTER =
          java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  //  private Journal journal;
  public final EventAnonymous<AppLogMessage> onNewMessage = new EventAnonymous<>();
  private LogFile logFile;

  public ApplicationLog() {
    this.logFile = null;
  }

  public void updateOutputFilePath(String fileName) {
    try {
      this.logFile = new LogFile(fileName);
    } catch (Exception e) {
      writeConsoleAndOptionallyFile(
              LogItemType.critical,
              "AppLog",
              sf(
                      "Failed to open log file '%s'. Reason: %s",
                      fileName,
                      ExceptionUtils.toFullString(e)),
              false
      );
      this.logFile = null;
    }
  }

  public void write(LogItemType type, String text) {
    this.write(type, null, text);
  }

  public void write(LogItemType type, String format, Object... params) {
    this.write(type, String.format(format, params));
  }

  public void write(LogItemType type, String sender, String text) {
    this.writeConsoleAndOptionallyFile(
            type, sender, text, true);
  }

  public void write(LogItemType type, String sender, String format, Object... params) {
    this.write(type, sender, String.format(format, params));
  }

  private void writeConsoleAndOptionallyFile(LogItemType type, String sender, String text, boolean writeToFile) {
    String txt = String.format(
            "%s [%-8s]; %-10s; %s",
            java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER),
            type,
            sender != null ? sender : "",
            text);


    if (writeToFile && logFile != null) {
      try {
        logFile.write(txt);
      } catch (Exception e) {
        writeConsoleAndOptionallyFile(
                LogItemType.critical,
                "AppLog", ExceptionUtils.toFullString(e, "==>"), false);
      }
    } // writeToFile

    System.out.println(txt);

    this.onNewMessage.raise(new AppLogMessage(type, txt));
  }
}
