package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.xml.traffic.TrafficXmlLoader;

public class TrafficXmlSource extends TrafficSource {

  private ITrafficModel traffic;
  private String fileName;

  public TrafficXmlSource(String fileName)
  {
    this.fileName = fileName;
  }

  @Override
  protected ITrafficModel _getContent() {
    return traffic;
  }

  @Override
  public void init() {
    try {
      this.traffic = TrafficXmlLoader.load(this.fileName);
    } catch (Exception e) {
      throw new EApplicationException("Unable to load traffic from file '" + this.fileName + "'.", e);
    } catch (Throwable t){
      throw new EApplicationException("Unable to load traffic from file '" + this.fileName + "'.", t);
    }

    super.setInitialized();
  }
}
