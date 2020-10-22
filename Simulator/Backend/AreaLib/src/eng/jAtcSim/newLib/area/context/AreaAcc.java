package eng.jAtcSim.newLib.area.context;

import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.RunwayConfiguration;

public class AreaAcc implements IAreaAcc {
  private final Airport airport;
  private final Area area;
  private final Producer<RunwayConfiguration> runwayConfigurationProducer;
  private final Producer<RunwayConfiguration> scheduledRunwayConfigurationProducer;

  public AreaAcc(Area area, Airport airport, Producer<RunwayConfiguration> runwayConfigurationProducer, Producer<RunwayConfiguration> scheduledRunwayConfigurationProducer) {
    EAssert.Argument.isNotNull(airport, "airport");
    EAssert.Argument.isNotNull(area, "area");
    EAssert.Argument.isNotNull(runwayConfigurationProducer, "runwayConfigurationProducer");
    EAssert.Argument.isNotNull(scheduledRunwayConfigurationProducer, "scheduledRunwayConfigurationProducer");

    this.airport = airport;
    this.area = area;
    this.runwayConfigurationProducer = runwayConfigurationProducer;
    this.scheduledRunwayConfigurationProducer = scheduledRunwayConfigurationProducer;
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
    return runwayConfigurationProducer.invoke();
  }

  @Override
  public RunwayConfiguration tryGetScheduledRunwayConfiguration() {
    return scheduledRunwayConfigurationProducer.invoke();
  }

  @Override
  public NavaidList getNavaids() {
    return area.getNavaids();
  }
}
