package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.exceptions.ApplicationException;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.xml.traffic.TrafficXmlLoader;
import exml.annotations.XConstructor;

public class TrafficXmlSource extends TrafficSource {

  private final String fileName;

  public String getFileName() {
    return fileName;
  }

  @XConstructor
  TrafficXmlSource(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public void init() {
    try {
      ITrafficModel t = TrafficXmlLoader.load(this.fileName);
      super.setContent(t);
    } catch (Exception e) {
      throw new ApplicationException("Unable to load traffic from file '" + this.fileName + "'.", e);
    } catch (Throwable t) {
      throw new ApplicationException("Unable to load traffic from file '" + this.fileName + "'.", t);
    }
  }
}
