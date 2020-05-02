package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.Navigator;
import eng.jAtcSim.newLib.airplanes.other.CockpitVoiceRecorder;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.mood.Mood;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoingAroundNotification;

public interface IAirplaneWriter {
  void abortHolding();

  void addExperience(Mood.ArrivalExperience experience);

  void addExperience(Mood.DepartureExperience experience);

  void applyShortcut(Navaid navaid);

  //TODO this should be replace with specific methods to start specific behaviors??
  //void changePilot(Pilot pilot, AirplaneState state);

  void clearedToApproach(Approach approach, ApproachEntry entry);

  void divert(boolean unknown);

  CockpitVoiceRecorder getCVR();

  void goAround(GoingAroundNotification.GoAroundReason reason);

  void hold(Navaid navaid, int inboundRadial, LeftRight turn);

  void processRadarContactConfirmation();

  void reportDivertTimeLeft();

  default void sendMessage(AtcId atcId, ISpeech speech){
    SpeechList<ISpeech> speeches = new SpeechList<>();
    speeches.add(speech);
    sendMessage(atcId, speeches);
  }

  void sendMessage(AtcId atcId, SpeechList<ISpeech> iSpeeches);

  void setAltitudeRestriction(Restriction restriction);

  void setRouting(IafRoute iafRoute, ActiveRunwayThreshold parent);
  void setRouting(DARoute daRoute, ActiveRunwayThreshold threshold);
  void setRouting(IReadOnlyList<ICommand> commands);

  void setSpeedRestriction(Restriction restriction);

  void setState(AirplaneState state);

  void setTargetAltitude(int altitudeInFt);

  void setTargetCoordinate(Coordinate coordinate);

  void setTargetHeading(Navigator navigator);

  void setTargetSpeed(int speed);

  void startArriving();

  void startDeparting();

  void startHolding(Navaid navaid, int inboundRadial, LeftRight turn);

  void startTakeOff(ActiveRunwayThreshold threshold);

  void tuneAtc(AtcId atc);
}
