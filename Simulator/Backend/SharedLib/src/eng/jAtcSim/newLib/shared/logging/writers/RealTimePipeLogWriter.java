package eng.jAtcSim.newLib.shared.logging.writers;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class RealTimePipeLogWriter extends PipeLogWriter {

  private static final String DATE_TIME_PATTERN = "yyyy-DD-mm HH:mm:ss";
  private static final String DELIMITER = " :: ";

  public RealTimePipeLogWriter(ILogWriter innerWriter) {
    super(innerWriter);
  }

  @Override
  public void write(String text) throws IOException {
    String timeText = java.time.LocalDateTime.now().format(
        DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    innerWriter.write(timeText + DELIMITER + text);
  }
}
