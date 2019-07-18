package eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.airplanes.pilots.navigators.INavigator;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.world.Navaid;

public interface IAirplaneWriteSimple extends IAirplaneRO {
  void addExperience(Mood.ArrivalExperience experience);

  void addExperience(Mood.DepartureExperience experience);

  void divert();

  void evaluateMoodForShortcut(Navaid navaid);

  void sendMessage(Atc atc, SpeechList saidText);

  void setAltitudeRestriction(Restriction restriction);

  void setNavigator(INavigator navigator);

  void setSpeedRestriction(Restriction speedRestriction);

  void setTakeOffPosition(Coordinate coordinate);

  void setTargetAltitude(int altitude);

  void setTargetSpeed(int ts);

  void setxState(Airplane.State state);
}
