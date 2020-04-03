package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public class GeneralAviationMovementTemplate extends MovementTemplate {
  private final String countryIcao;
  public GeneralAviationMovementTemplate(
      eKind kind, ETimeStamp time, String countryIcao,
      EntryExitInfo entryExitInfo) {
    super(kind, time, entryExitInfo);
    this.countryIcao = countryIcao;
  }

  public String getCountryIcao() {
    return countryIcao;
  }
}
