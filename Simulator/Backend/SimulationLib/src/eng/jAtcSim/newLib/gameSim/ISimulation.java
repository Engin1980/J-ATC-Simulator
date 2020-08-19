package eng.jAtcSim.newLib.gameSim;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.events.IEventListenerSimple;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.gameSim.simulation.IScheduledMovement;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;
import eng.jAtcSim.newLib.stats.IStatsProvider;

public interface ISimulation {
  Airport getAirport();

  ApplicationLog getAppLog();

  Area getArea();

  AtcList<AtcId> getAtcs();

  IList<Message> getMessages(Object key);

  EDayTimeStamp getNow();

  IParseFormat getParseFormat();

  IReadOnlyList<IAirplaneInfo> getPlanesToDisplay();

  RunwayConfiguration getRunwayConfigurationInUse();

  IReadOnlyList<IScheduledMovement> getScheduledMovements();

  IStatsProvider getStats();

  AtcId getUserAtcId();

  void pauseUnpauseSim();

  void registerMessageListener(Object listener, Messenger.ListenerAim... aims);

  int registerOnRunwayChanged(IEventListenerSimple<ISimulation> action);

  int registerOnSecondElapsed(IEventListenerSimple<ISimulation> action);

  void sendAtcCommand(AtcId id, IAtcSpeech speech);

  void sendPlaneCommands(Callsign callsign, SpeechList<IForPlaneSpeech> cmds);

  void sendSystemCommand(ISystemSpeech speech);

  void start();

  void stop();

  void unregisterMessageListener(Object listener);

  void unregisterOnSecondElapsed(int simulationSecondListenerHandlerId);
}
