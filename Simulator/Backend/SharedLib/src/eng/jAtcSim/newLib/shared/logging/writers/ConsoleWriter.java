package eng.jAtcSim.newLib.shared.logging.writers;

import java.io.IOException;

public class ConsoleWriter implements ILogWriter {

  private final boolean autoNewLine;

  public ConsoleWriter(boolean autoNewLine) {
    this.autoNewLine = autoNewLine;
  }

  public ConsoleWriter() {
    this.autoNewLine = true;
  }

  @Override
  public void close() {
  }

  @Override
  public void write(String text) throws IOException {
    if (autoNewLine)
      System.out.println(text);
    else
      System.out.print(text);
  }
}
