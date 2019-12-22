package eng.jAtcSim.newLib.shared.logging.writers;

import eng.eSystem.validation.EAssert;

import java.io.BufferedWriter;
import java.io.IOException;

public class FileWriter implements ILogWriter {
  private final String fileName;
  private final boolean autoNewLine;
  private BufferedWriter bw = null;

  public FileWriter(String fileName, boolean autoNewLine) {
    EAssert.isNotNull(fileName, "Value of {fileName} cannot not be null.");
    this.fileName = fileName;
    this.autoNewLine = autoNewLine;
  }

  @Override
  public void write(String text) throws IOException {
    if (bw == null)
      openWriter();
    bw.write(text);
    if (autoNewLine)
      bw.newLine();
    bw.flush();
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

  private void openWriter() throws IOException {
    try {
      bw = new BufferedWriter(new java.io.FileWriter(this.fileName));
    } catch (IOException ex) {
      throw new IOException("Unable to open a file " + this.fileName + " for writing.", ex);
    }
  }
}
