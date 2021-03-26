package eng.jAtcSim.shared;

import javax.swing.*;

public class BackgroundWorker<T> extends Thread {
  public interface Work<T> {
    T doJob();
  }
  public interface Finished<T> {
    void finished(T result, Exception ex);
  }
  private final Work work;
  private final Finished finished;

  public BackgroundWorker(Work<T> work, Finished<T> finished) {
    this.work = work;
    this.finished = finished;
  }

  @Override
  public void run() {
    T ret;
    Exception exception;

    try {
      ret = (T) work.doJob();
      exception = null;
    } catch (Exception ex) {
      ret = null;
      exception = ex;
    }

    final T a = ret;
    final Exception b = exception;

    SwingUtilities.invokeLater(() -> this.finished.finished(a, b));
  }
}
