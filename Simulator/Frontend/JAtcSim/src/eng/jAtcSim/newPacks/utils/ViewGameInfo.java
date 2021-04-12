package eng.jAtcSim.newPacks.utils;

import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.DynamicPlaneFormatter;
import eng.jAtcSim.settings.AppSettings;

public class ViewGameInfo {
  private ISimulation simulation;
  private AppSettings settings;
  private AtcId userAtcId;
  private Airport airport;
  private DynamicPlaneFormatter dynamicAirplaneSpeechFormatter;

  public ViewGameInfo() {
  }

  public Airport getAirport() {
    return airport;
  }

  public void setAirport(Airport airport) {
    this.airport = airport;
  }

  public DynamicPlaneFormatter getDynamicAirplaneSpeechFormatter() {
    return dynamicAirplaneSpeechFormatter;
  }

  public void setDynamicAirplaneSpeechFormatter(DynamicPlaneFormatter dynamicAirplaneSpeechFormatter) {
    this.dynamicAirplaneSpeechFormatter = dynamicAirplaneSpeechFormatter;
  }

  public AppSettings getSettings() {
    return settings;
  }

  public void setSettings(AppSettings settings) {
    this.settings = settings;
  }

  public ISimulation getSimulation() {
    return simulation;
  }

  public void setSimulation(ISimulation simulation) {
    this.simulation = simulation;
  }

  public AtcId getUserAtcId() {
    return this.userAtcId;
  }

  public void setUserAtcId(AtcId userAtcId) {
    this.userAtcId = userAtcId;
  }
}
