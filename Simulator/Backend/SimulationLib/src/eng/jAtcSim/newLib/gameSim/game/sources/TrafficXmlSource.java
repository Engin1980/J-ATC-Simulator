package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.xml.traffic.TrafficXmlLoader;

public class TrafficXmlSource extends TrafficSource {

  private final String fileName;

  public String getFileName() {
    return fileName;
  }

  TrafficXmlSource(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public void init() {
    try {
      ITrafficModel t = TrafficXmlLoader.load(this.fileName);
      super.setContent(t);
    } catch (Exception e) {
      throw new EApplicationException("Unable to load traffic from file '" + this.fileName + "'.", e);
    } catch (Throwable t) {
      throw new EApplicationException("Unable to load traffic from file '" + this.fileName + "'.", t);
    }
  }
}
