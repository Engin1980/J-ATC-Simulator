package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;
import eng.jAtcSim.newLib.speeches.INotification;

public abstract class NotificationApplication<T extends INotification> {

  public abstract ConfirmationResult confirm(IPlaneInterface plane, T c);

  public abstract ApplicationResult apply(IPlaneInterface plane, T c);
}
