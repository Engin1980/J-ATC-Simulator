package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcNotification;

public abstract class NotificationApplication<T extends IAtcNotification> {

  public abstract ConfirmationResult confirm(Pilot.Pilot5Command pilot, T c);

  public abstract ApplicationResult apply(Pilot.Pilot5Command pilot, T c);
}
