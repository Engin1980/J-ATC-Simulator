package eng.jAtcSim.newLib.shared.logging.writers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.utilites.ExceptionUtils;

import java.io.IOException;

public class BroadcastingWriter implements ILogWriter {

  private final IList<ILogWriter> writers;

  public BroadcastingWriter(IList<ILogWriter> writers) {
    this.writers = writers;
  }

  @Override
  public void close() {
    writers.forEach(q -> q.close());
  }

  @Override
  public void write(String text) {
    IList<IOException> exceptions = new EList<>();
    for (ILogWriter writer : writers) {
      try {
        writer.write(text);
      } catch (IOException ex) {
        exceptions.add(ex);
      }
    }
    if (exceptions.isEmpty() == false) {
      EStringBuilder sb = new EStringBuilder();
      for (IOException exception : exceptions) {
        if (exception != exceptions.getFirst()) {
          sb.append(" |||---||| ");
        }
        String s = ExceptionUtils.toFullString(exception, "\n\t\t");
        sb.append(s);
      }
    }
  }
}
