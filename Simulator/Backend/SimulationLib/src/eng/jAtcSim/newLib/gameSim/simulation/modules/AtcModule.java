package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.jAtcSim.newLib.atcs.AtcProvider;

public class AtcModule {
  private final AtcProvider atcProvider;

  public AtcModule(AtcProvider atcProvider) {
    this.atcProvider = atcProvider;
  }
}
