package eng.jAtcSim.newPacks;

import eng.eSystem.events.Event;
import eng.jAtcSim.newLib.shared.Callsign;

public interface ICanSelectCallsign {
  Event<IView, Callsign> onSelectedCallsignChanged();
  void setSelectedCallsign(Callsign callsign);
  Callsign getSelectedCallsign();
}
