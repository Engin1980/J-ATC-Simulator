package eng.jAtcSim.newLib.shared.logging.writers;

import eng.eSystem.validation.EAssert;

import java.io.IOException;

public abstract class PipeLogWriter implements ILogWriter {
  protected final ILogWriter innerWriter;

  public PipeLogWriter(ILogWriter innerWriter) {
    EAssert.isNotNull(innerWriter);
    this.innerWriter = innerWriter;
  }

  @Override
  public void close() {
    innerWriter.close();
  }

  @Override
  public void newLine() throws IOException {
    innerWriter.newLine();
  }

  @Override
  public abstract void write(String text) throws IOException;
}
