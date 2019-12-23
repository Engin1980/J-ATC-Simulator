package eng.jAtcSim.newLib.shared.logging.writers;

import java.io.IOException;

public interface ILogWriter {
  void write(String text) throws IOException;
  void newLine() throws IOException;
  default void writeLine(String text) throws IOException{
    this.write(text);
    this.newLine();
  }
  void close();
}
