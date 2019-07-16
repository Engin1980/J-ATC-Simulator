package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.airplanes.pilots.navigators.INavigator;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;

public interface IPilot5Behavior extends IPilot5 {

  void addExperience(Mood.ArrivalExperience experience);

  void addExperience(Mood.DepartureExperience experience);

  void goAround(GoingAroundNotification.GoAroundReason reason,
                double course,
                SpeechList gaRoute);

  void setNavigator(INavigator navigator);

  void setRoute(SpeechList route);
}
