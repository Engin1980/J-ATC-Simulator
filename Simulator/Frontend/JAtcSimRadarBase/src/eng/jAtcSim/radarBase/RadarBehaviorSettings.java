package eng.jAtcSim.radarBase;

import eng.jAtcSim.newLib.area.textProcessing.formatting.Formatter;
import eng.jAtcSim.newLib.area.textProcessing.formatting.IFormatter;

public class RadarBehaviorSettings {
  private final boolean paintMessages;
  private final IFormatter formatter;

  public boolean isPaintMessages() {
    return paintMessages;
  }

  public IFormatter getFormatter() {
    return formatter;
  }

  public RadarBehaviorSettings(boolean paintMessages, IFormatter formatter) {
    if (formatter == null) {
        throw new IllegalArgumentException("Value of {formatter} cannot not be null.");
    }

    this.paintMessages = paintMessages;
    this.formatter = formatter;
  }

  public RadarBehaviorSettings(Formatter formatter) {
    this(true, formatter);
  }

}
