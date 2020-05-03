package eng.jAtcSim.newLib.shared.logging;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.ExceptionUtils;
import eng.jAtcSim.newLib.shared.logging.writers.ILogWriter;

import java.io.IOException;
import java.util.Arrays;

public final class Journal {

  private final IReadOnlyList<ILogWriter> writers;
  private final String name;
  private final boolean errorForgiving;

  public Journal(String name, boolean errorForgiving, ILogWriter... outputStreams) {
    ILogWriter[] writers = Arrays.copyOf(outputStreams, outputStreams.length);
    this.writers = new EList<>(writers);
    this.name = name;
    this.errorForgiving = errorForgiving;
  }

  public void close() {
    for (ILogWriter writer : writers) {
      writer.close();
    }
  }

  public String getName() {
    return name;
  }

  public void write(String format, Object... params) {
    String tmp = prepareText(format, params);
    writers.forEach(q -> tryWriteToLog(q, tmp));
  }

  public void writeLine(String format, Object... params) {
    String tmp = prepareText(format, params);
    writers.forEach(q -> tryWriteLineToLog(q, tmp));
  }

  private String prepareText(String format, Object[] params) {
    String tmp;
    if (params.length == 0)
      tmp = format;
    else
      tmp = String.format(format, params);
    return tmp;
  }

  private void processError(IOException ex) {
    if (errorForgiving)
      System.out.println("Log " + this.name + " writing failed: " + ExceptionUtils.toFullString(ex, " ==> "));
    else
      throw new ERuntimeException("Log " + this.name + " writing failed.", ex);
  }

  private void tryWriteLineToLog(ILogWriter wrt, String value) {
    try {
      wrt.writeLine(value);
    } catch (IOException ex) {
      processError(ex);
    }
  }

  private void tryWriteToLog(ILogWriter wrt, String value) {
    try {
      wrt.write(value);
    } catch (IOException ex) {
      processError(ex);
    }
  }
}
