package eng.jAtcSim.newLib.gameSim.game.startupInfos;

public class AreaStartupSourceInfo {
  public String areaXmlFile;
  public String icao;

  public AreaStartupSourceInfo(String areaXmlFile, String icao) {
    this.areaXmlFile = areaXmlFile;
    this.icao = icao;
  }

  public AreaStartupSourceInfo() {
  }
}
