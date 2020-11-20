package eng.jAtcSim.newLib.area.context;

import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.RunwayConfiguration;

public class AreaAcc implements IAreaAcc {
  private final Airport airport;
  private final Area area;
  private RunwayConfiguration currentRunwayConfiguration;
  private RunwayConfiguration scheduledRunwayConfiguration;
  private final EventAnonymousSimple onCurrentRunwayConfigurationChange = new EventAnonymousSimple();
  private final EventAnonymousSimple onScheduledRunwayConfigurationChange = new EventAnonymousSimple();

  public AreaAcc(Area area, Airport airport) {
    EAssert.Argument.isNotNull(airport, "airport");
    EAssert.Argument.isNotNull(area, "area");

    this.airport = airport;
    this.area = area;
  }

  @Override
  public Airport getAirport() {
    return airport;
  }

  @Override
  public Area getArea() {
    return area;
  }

  @Override
  public RunwayConfiguration getCurrentRunwayConfiguration() {
    return currentRunwayConfiguration;
  }

  @Override
  public RunwayConfiguration tryGetScheduledRunwayConfiguration() {
    return scheduledRunwayConfiguration;
  }

  public void setCurrentRunwayConfiguration(RunwayConfiguration currentRunwayConfiguration) {
    if (this.currentRunwayConfiguration != currentRunwayConfiguration) {
      this.currentRunwayConfiguration = currentRunwayConfiguration;
      this.onCurrentRunwayConfigurationChange.raise();
    }
  }

  public void setScheduledRunwayConfiguration(RunwayConfiguration scheduledRunwayConfiguration) {
    if (this.scheduledRunwayConfiguration != scheduledRunwayConfiguration) {
      this.scheduledRunwayConfiguration = scheduledRunwayConfiguration;
      this.onScheduledRunwayConfigurationChange.raise();
    }
  }

  @Override
  public NavaidList getNavaids() {
    return area.getNavaids();
  }

  @Override
  public EventAnonymousSimple onCurrentRunwayConfigurationChange() {
    return this.onCurrentRunwayConfigurationChange;
  }

  @Override
  public EventAnonymousSimple onScheduledRunwayConfigurationChange() {
    return this.onScheduledRunwayConfigurationChange;
  }
}
