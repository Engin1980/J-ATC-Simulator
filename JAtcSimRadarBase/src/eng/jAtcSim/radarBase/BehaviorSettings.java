package eng.jAtcSim.radarBase;

import eng.jAtcSim.lib.speaking.formatting.Formatter;

public class BehaviorSettings {
  private final boolean paintMessages;
  private final Formatter formatter;
  private final int displayTextDelay;

  public boolean isPaintMessages() {
    return paintMessages;
  }

  public Formatter getFormatter() {
    return formatter;
  }

  public BehaviorSettings(boolean paintMessages, Formatter formatter, int displayTextDelay) {
    if (formatter == null) {
        throw new IllegalArgumentException("Value of {formatter} cannot not be null.");
    }

    this.paintMessages = paintMessages;
    this.formatter = formatter;
    this.displayTextDelay = displayTextDelay;
  }

  public BehaviorSettings(Formatter formatter) {
    this(true, formatter, 5);
  }

  public int getDisplayTextDelay() {
    return displayTextDelay;
  }
}
