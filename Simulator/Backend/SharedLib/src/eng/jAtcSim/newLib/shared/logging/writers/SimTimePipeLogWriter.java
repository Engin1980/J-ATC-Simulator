package eng.jAtcSim.newLib.shared.logging.writers;

import eng.jAtcSim.newLib.shared.SharedFactory;

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
    String timeText = SharedFactory.getNow().toDayTimeString();
    innerWriter.write(timeText + delimiter + text);
  }
}
