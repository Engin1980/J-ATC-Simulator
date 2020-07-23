package eng.jAtcSim.newLib.xml.area.internal.context;

import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.area.context.IAreaAcc;

public class LoadingAreaAcc implements IAreaAcc {

  private final NavaidList navaids;

  public LoadingAreaAcc(NavaidList navaids) {
    this.navaids = navaids;
  }

  @Override
  public Airport getAirport() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Area getArea() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RunwayConfiguration getCurrentRunwayConfiguration() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RunwayConfiguration tryGetScheduledRunwayConfiguration() {
    throw new UnsupportedOperationException();
  }

  @Override
  public NavaidList getNavaids() {
    return this.navaids;
  }
}
