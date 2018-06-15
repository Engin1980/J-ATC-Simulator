package eng.jAtcSim.lib.global.logging;

import java.io.*;

public class WriterSaver extends AbstractSaver {

  private final boolean canBeClosed;
  private BufferedWriter bw;

  public WriterSaver(OutputStream os, boolean canBeClosed) {
    if (os == null) {
      throw new IllegalArgumentException("Value of {os} cannot not be null.");
    }
    this.bw = new BufferedWriter(new OutputStreamWriter(os));
    this.canBeClosed = canBeClosed;
  }

  @Override
  public void write(String text) throws IOException {
    bw.write(text);
    bw.flush();
  }

  @Override
  public void close() {
    if (canBeClosed)
      try {
        bw.close();
      } catch (IOException ex) {
      } finally {
        bw = null;
      }
  }
}
