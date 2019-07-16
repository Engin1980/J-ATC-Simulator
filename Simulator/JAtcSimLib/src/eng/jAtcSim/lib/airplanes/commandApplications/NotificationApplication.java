package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Command;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcNotification;

public abstract class NotificationApplication<T extends IAtcNotification> {

  public abstract ConfirmationResult confirm(IPilot5Command pilot, T c);

  public abstract ApplicationResult apply(IPilot5Command pilot, T c);
}
