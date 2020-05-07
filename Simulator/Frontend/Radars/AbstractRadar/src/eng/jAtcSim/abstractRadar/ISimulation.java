package eng.jAtcSim.abstractRadar;

import eng.eSystem.collections.*;
import eng.eSystem.events.EventSimple;
import eng.jAtcSim.newLib.airplanes.IAirplaneSHA;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public interface ISimulation {
  Airport airport();

  ApplicationLog getAppLog();

  EDayTimeStamp getNow();

  EventSimple<ISimulation> getOnRunwayChanged();

  EventSimple<Radar> getOnSecondElapsed() ;

  IReadOnlyList<IAirplaneInfo> getPlanesToDisplay();

  RunwayConfiguration getRunwayConfigurationInUse();
}
