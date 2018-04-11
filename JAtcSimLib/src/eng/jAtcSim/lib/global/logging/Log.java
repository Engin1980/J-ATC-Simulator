package eng.jAtcSim.lib.global.logging;

import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.ExceptionUtil;

import java.io.*;
import java.nio.charset.Charset;

public abstract class Log {

  private static final Charset CHARSET = Charset.forName("UTF-8");
  private final OutputStreamWriter[] writers;
  private final String name;
  private final boolean errorKind;

  public String getName() {
    return name;
  }

  public Log(String name, boolean errorKind, OutputStream... outputStreams) {
    this.writers = new OutputStreamWriter[outputStreams.length];
    for (int i = 0; i < this.writers.length; i++) {
      this.writers[i] = new OutputStreamWriter(outputStreams[i], CHARSET);
    }
    this.name = name;
    this.errorKind = errorKind;
  }

  public static OutputStream openFileStream(String fileName) {
    OutputStream ret;
    try {
      ret = new BufferedOutputStream(new FileOutputStream((fileName)));
    } catch (IOException e) {
      throw new ERuntimeException("Failed to open file " + fileName + " as write for writing.");
    }
    return ret;
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
    for (OutputStreamWriter os : writers) {
      tryWrite(os, tmp);
    }
  }

  private void tryWrite(OutputStreamWriter wrt, String tmp) {
    try {
      wrt.write(tmp);
      wrt.flush();
    } catch (IOException ex) {
      if (errorKind)
        System.out.println("Log " + this.name + " write failed: " + ExceptionUtil.toFullString(ex, " ==> "));
      else
        throw new ERuntimeException("Log " + this.name + " write failed.", ex);
    }
  }
}
