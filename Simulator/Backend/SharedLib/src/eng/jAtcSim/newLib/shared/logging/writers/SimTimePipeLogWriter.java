package eng.jAtcSim.newLib.shared.logging.writers;

import eng.jAtcSim.newLib.shared.context.SharedAcc;

import java.io.IOException;

public class SimTimePipeLogWriter extends PipeLogWriter {

  private final String delimiter;

  public SimTimePipeLogWriter(ILogWriter innerWriter, String delimiter) {
    super(innerWriter);
    this.delimiter = delimiter;
  }

  public SimTimePipeLogWriter(ILogWriter innerWriter) {
    this(innerWriter, "; ");
  }

  @Override
  public void write(String text) throws IOException {
    String timeText = SharedAcc.getNow().toString();
    innerWriter.write(timeText + delimiter + text);
  }
}
