package eng.jAtcSim.newLib.shared.logging.writers;

import eng.eSystem.validation.EAssert;

import java.io.BufferedWriter;
import java.io.IOException;

public class FileWriter implements ILogWriter {
  private final String fileName;
  private final boolean autoFlush;
  private BufferedWriter bw = null;

  public FileWriter(String fileName, boolean autoFlush) {
    EAssert.isNotNull(fileName, "Value of {fileName} cannot not be null.");
    this.fileName = fileName;
    this.autoFlush = autoFlush;
  }

  public FileWriter(String fileName) {
    this(fileName, true);
  }

  @Override
  public void close() {
    if (bw != null)
      try {
        bw.close();
      } catch (IOException ex) {
      } finally {
        bw = null;
      }
  }

  @Override
  public void newLine() throws IOException {
    bw.newLine();
    if (autoFlush) bw.flush();
  }

  @Override
  public void write(String text) throws IOException {
    if (bw == null)
      openWriter();
    bw.write(text);
    if (autoFlush) bw.flush();
  }

  private void openWriter() throws IOException {
    try {
      bw = new BufferedWriter(new java.io.FileWriter(this.fileName));
    } catch (IOException ex) {
      throw new IOException("Unable to open a file " + this.fileName + " for writing.", ex);
    }
  }
}
