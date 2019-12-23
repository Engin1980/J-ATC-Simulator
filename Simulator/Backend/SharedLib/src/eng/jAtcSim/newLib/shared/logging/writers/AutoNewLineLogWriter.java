package eng.jAtcSim.newLib.shared.logging.writers;

import java.io.IOException;

public class AutoNewLineLogWriter extends PipeLogWriter {

  public AutoNewLineLogWriter(ILogWriter innerWriter) {
    super(innerWriter);
  }

  @Override
  public void write(String text) throws IOException {
    super.innerWriter.writeLine(text);
  }
}
