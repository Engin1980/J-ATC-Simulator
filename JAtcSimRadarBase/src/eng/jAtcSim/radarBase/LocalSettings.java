package eng.jAtcSim.radarBase;

import eng.eSystem.events.EventSimple;

public class LocalSettings {
  private eng.eSystem.events.EventSimple<LocalSettings> updatedEvent =
      new EventSimple<>(this);

  public EventSimple<LocalSettings> getUpdatedEvent() {
    return updatedEvent;
  }
}
