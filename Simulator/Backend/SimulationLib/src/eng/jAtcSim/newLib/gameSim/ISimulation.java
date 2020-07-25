package eng.jAtcSim.newLib.gameSim;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.events.EventSimple;
import eng.eSystem.events.IEventListenerSimple;
import eng.eSystem.functionalInterfaces.Action;
import eng.eSystem.functionalInterfaces.Action1;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.gameSim.simulation.IScheduledMovement;
import eng.jAtcSim.newLib.messaging.Participant;
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

  IList<IMessage> getMessages(Object key);

  EDayTimeStamp getNow();

  int registerOnRunwayChanged(IEventListenerSimple<ISimulation> action);

  IParseFormat getParseFormat();

  IReadOnlyList<IAirplaneInfo> getPlanesToDisplay();

  RunwayConfiguration getRunwayConfigurationInUse();

  IReadOnlyList<IScheduledMovement> getScheduledMovements();

  IStatsProvider getStats();

  AtcId getUserAtcId();

  void pauseUnpauseSim();

  default void registerMessageListenerByReceiver(Object key, Callsign recieverCallsign) {
    registerMessageListenerByReceiver(key, Participant.createAirplane(recieverCallsign));
  }

  default void registerMessageListenerByReceiver(Object key, AtcId recieverAtc) {
    registerMessageListenerByReceiver(key, Participant.createAtc(recieverAtc));
  }

  void registerMessageListenerByReceiver(Object key, Participant messageReceiver);

  default void registerMessageListenerBySender(Object key, Callsign senderCallsign) {
    registerMessageListenerByReceiver(key, Participant.createAirplane(senderCallsign));
  }

  default void registerMessageListenerBySender(Object key, AtcId senderAtc) {
    registerMessageListenerByReceiver(key, Participant.createAtc(senderAtc));
  }

  void registerMessageListenerBySender(Object key, Participant messageSender);

  int registerOnSecondElapsed(IEventListenerSimple<ISimulation> action);

  void sendAtcCommand(AtcId id, IAtcSpeech speech);

  void sendPlaneCommands(Callsign callsign, SpeechList<IForPlaneSpeech> cmds);

  void sendSystemCommand(ISystemSpeech speech);

  void start();

  void stop();

  void unregisterOnSecondElapsed(int simulationSecondListenerHandlerId);
}
