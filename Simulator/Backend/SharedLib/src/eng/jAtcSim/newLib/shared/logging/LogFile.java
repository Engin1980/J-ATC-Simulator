package eng.jAtcSim.newLib.shared.logging;

import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.contextLocal.Context;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class LogFile implements AutoCloseable {
  public static LogFile openInDefaultPath(String fileName) {
    return openInDefaultPath(fileName, true);
  }
  public static LogFile openInDefaultPath(String fileName, boolean autoFlush) {
    Path logPath = Context.getApp().getLogPath();
    Path tmp = logPath.resolve(fileName);
    LogFile ret = new LogFile(tmp.toString(), autoFlush);
    return ret;
  }
  private final boolean autoFlush;
  private BufferedWriter bw = null;
  private final String fileName;

  public LogFile(String fileName, boolean autoFlush) {
    EAssert.isNotNull(fileName, "Value of {fileName} cannot not be null.");
    this.fileName = fileName;
    this.autoFlush = autoFlush;
  }

  public LogFile(String fileName) {
    this(fileName, true);
  }

  @Override
  public void close() {
    if (bw != null)
      try {
        bw.close();
      } catch (IOException ex) {
      } finally {
        bw = null;
      }
  }

  public void write(String format, Object...params){
    this.write(String.format(format, params));
  }

  public void write(String text) {
    try {
      if (bw == null)
        openWriter();
      bw.write(text);
      if (autoFlush) bw.flush();
    } catch (IOException e) {
      throw new ApplicationException("Failed to write into log file.", e);
    }
  }

  private String getFullFileName() {
    Path ret;
    if (Paths.get(this.fileName).isAbsolute() == false) {
      Path parent = Context.getApp().getLogPath();
      ret = parent.resolve(this.fileName);
    } else
      ret = Paths.get(this.fileName);
    return ret.toAbsolutePath().toString();
  }

  private void openWriter() throws IOException {
    try {
      bw = new BufferedWriter(new java.io.FileWriter(this.getFullFileName()));
    } catch (IOException ex) {
      throw new IOException(sf("Unable to open a file %s (%s) for writing.", this.fileName, this.getFullFileName()), ex);
    }
  }
}
