package eng.jAtcSim.abstractRadar.settings;

import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.DynamicPlaneFormatter;

public class RadarBehaviorSettings {
  private final boolean paintMessages;

  public boolean isPaintMessages() {
    return paintMessages;
  }

  public RadarBehaviorSettings(boolean paintMessages) {
    this.paintMessages = paintMessages;
  }
}
