package eng.jAtcSim.radarBase;

import eng.jAtcSim.lib.speaking.formatting.Formatter;

public class BehaviorSettings {
  private final boolean paintMessages;
  private final Formatter formatter;

  public boolean isPaintMessages() {
    return paintMessages;
  }

  public Formatter getFormatter() {
    return formatter;
  }

  public BehaviorSettings(boolean paintMessages, Formatter formatter) {
    if (formatter == null) {
        throw new IllegalArgumentException("Value of {formatter} cannot not be null.");
    }

    this.paintMessages = paintMessages;
    this.formatter = formatter;
  }

  public BehaviorSettings(Formatter formatter) {
    this(true, formatter);
  }

}
