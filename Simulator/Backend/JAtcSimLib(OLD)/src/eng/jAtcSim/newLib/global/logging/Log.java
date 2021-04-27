package eng.jAtcSim.newLib.area.global.logging;

import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.utilites.ExceptionUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;

public abstract class Log {

  private final AbstractSaver[] writers;
  private final String name;
  private final boolean errorKind;

  public String getName() {
    return name;
  }

  public Log(String name, boolean errorKind, AbstractSaver ... outputStreams) {
    this.writers = Arrays.copyOf(outputStreams, outputStreams.length);
    this.name = name;
    this.errorKind = errorKind;
  }

  protected void writeLine(String format, Object... params) {
    this.write(format, params);
    this.write("\n");
  }

  private void write(String format, Object... params) {
    String tmp;
    if (params.length == 0)
      tmp = format;
    else
      tmp = String.format(format, params);
    for (AbstractSaver os : writers) {
      tryWrite(os, tmp);
    }
  }

  public void close(){
    for (AbstractSaver writer : writers) {
      writer.close();
    }
  }

  private void tryWrite(AbstractSaver wrt, String tmp) {
    try {
      wrt.write(tmp);
    } catch (IOException ex) {
      if (errorKind)
        System.out.println("Log " + this.name + " write failed: " + ExceptionUtils.toFullString(ex, " ==> "));
      else
        throw new ApplicationException("Log " + this.name + " write failed.", ex);
    }
  }
}
