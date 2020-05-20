package eng.jAtcSim.newLib.gameSim;

import eng.eSystem.collections.*;
import eng.eSystem.events.EventSimple;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public interface ISimulation {
  Airport airport();

  ApplicationLog getAppLog();

  IList<AtcId> getAtcs();

  EDayTimeStamp getNow();

  EventSimple<ISimulation> getOnRunwayChanged();

  EventSimple<?> getOnSecondElapsed();

  IReadOnlyList<IAirplaneInfo> getPlanesToDisplay();

  RunwayConfiguration getRunwayConfigurationInUse();

  void registerMessageListenerByReceiver(Object key, Participant messageReceiver);
  void registerMessageListenerBySender(Object key, Participant messageSender);
  IList<IMessage> getMessages(Object key);
}
