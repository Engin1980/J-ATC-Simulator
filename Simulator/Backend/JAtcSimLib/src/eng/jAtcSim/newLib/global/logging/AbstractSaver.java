package eng.jAtcSim.newLib.area.global.logging;

import java.io.IOException;

public abstract class AbstractSaver {
  public abstract void write(String text) throws IOException;
  public abstract void close();
}
