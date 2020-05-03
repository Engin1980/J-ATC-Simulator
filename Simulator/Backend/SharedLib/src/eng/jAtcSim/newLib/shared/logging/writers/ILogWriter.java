package eng.jAtcSim.newLib.shared.logging.writers;

import java.io.IOException;

public interface ILogWriter {
  void close();

  default void newLine() throws IOException {
    write("\r\n");
  }

  void write(String text) throws IOException;

  default void writeLine(String text) throws IOException {
    this.write(text);
    this.newLine();
  }
}
