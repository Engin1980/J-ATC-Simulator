package eng.jAtcSim.abstractRadar.settings;

public class RadarBehaviorSettings {
  private final boolean paintMessages;

  public boolean isPaintMessages() {
    return paintMessages;
  }

  public RadarBehaviorSettings(boolean paintMessages) {
    this.paintMessages = paintMessages;
  }
}