package eng.jAtcSim.newLib.shared.logging.writers;

import java.io.IOException;

public interface ILogWriter {
  void write(String text) throws IOException;
  void close();
}
