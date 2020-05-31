package eng.jAtcSim.newLib.gameSim;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.events.EventSimple;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
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
import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParser;
import eng.jAtcSim.newLib.textProcessing.parsing.IPlaneParser;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParser;

public interface ISimulation {
  Airport getAirport();

  ApplicationLog getAppLog();

  Area getArea();

  IAtcParser getAtcParser();

  IReadOnlyList<AtcId> getAtcs();

  IList<IMessage> getMessages(Object key);

  EDayTimeStamp getNow();

  EventSimple<ISimulation> getOnRunwayChanged();

  EventSimple<?> getOnSecondElapsed();

  IPlaneParser getPlaneParser();

  IReadOnlyList<IAirplaneInfo> getPlanesToDisplay();

  RunwayConfiguration getRunwayConfigurationInUse();

  IReadOnlyList<IScheduledMovement> getScheduledMovements();

  ISystemParser getSystemParser();

  void pauseUnpauseSim();

  void registerMessageListenerByReceiver(Object key, Participant messageReceiver);

  void registerMessageListenerBySender(Object key, Participant messageSender);

  void sendAtcCommand(AtcId id, IAtcSpeech speech);

  void sendPlaneCommands(Callsign callsign, SpeechList<IForPlaneSpeech> cmds);

  void sendSystemCommand(ISystemSpeech speech);

  void start();
}
