package eng.jAtcSim.newLib.shared.logging.writers;

import java.io.IOException;

public class ConsoleWriter implements ILogWriter {

  @Override
  public void close() {
  }

  @Override
  public void newLine() throws IOException {
    System.out.println();
  }

  @Override
  public void write(String text) throws IOException {
    System.out.print(text);
  }

  @Override
  public void writeLine(String text) throws IOException {
    System.out.println(text);
  }
}
