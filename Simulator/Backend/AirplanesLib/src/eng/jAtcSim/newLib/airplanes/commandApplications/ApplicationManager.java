package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;
import eng.jAtcSim.newLib.speeches.Confirmation;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.INotification;
import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterCommand;

public class ApplicationManager {

  private static IMap<Class<? extends ICommand>, CommandApplication> cmdApps;
  private static IMap<Class<? extends INotification>, NotificationApplication> notApps;

  public static ConfirmationResult confirm(IPlaneInterface plane, ISpeech speech, boolean checkStateSanity, boolean checkCommandSanity) {
    ConfirmationResult ret;

    if (speech instanceof AfterCommand) {
      ret = new ConfirmationResult();
      ret.confirmation = new Confirmation((ICommand) speech);
    } else if (speech instanceof ICommand) {
      ICommand command = (ICommand) speech;
      CommandApplication ca = cmdApps.get(command.getClass());
      assert ca != null : "Unknown application. Probably not added into cmdApps list?";
      assert plane != null;
      assert command != null;
      ret = ca.confirm(plane, command, checkStateSanity, checkCommandSanity);
    } else if (speech instanceof INotification) {
      INotification notification = (INotification) speech;
      NotificationApplication na = notApps.get(notification.getClass());
      assert na != null;
      ret = na.confirm(plane, notification);
    } else {
      throw new UnsupportedOperationException();
    }

    return ret;

  }

  public static ApplicationResult apply(IPlaneInterface plane, ISpeech speech) {
    ApplicationResult ret;

    if (speech instanceof ICommand) {
      ICommand command = (ICommand) speech;
      ret = cmdApps.get(command.getClass()).apply(plane, command, false);
    } else if (speech instanceof INotification) {
      INotification notification = (INotification) speech;
      ret = notApps.get(notification.getClass()).apply(plane, notification);
    } else {
      throw new UnsupportedOperationException();
    }

    return ret;
  }

  static {
    cmdApps = new EMap<>();

    cmdApps.set(ChangeAltitudeCommand.class, new ChangeAltitudeApplication());
    cmdApps.set(ChangeHeadingCommand.class, new ChangeHeadingApplication());
    cmdApps.set(ChangeSpeedCommand.class, new ChangeSpeedApplication());
    cmdApps.set(ClearedForTakeoffCommand.class, new ClearedForTakeOffCommandApplication());
    cmdApps.set(ClearedToApproachCommand.class, new ClearedToApproachApplication());
    cmdApps.set(HoldCommand.class, new HoldCommandApplication());
    cmdApps.set(ProceedDirectCommand.class, new ProceedDirectApplication());
    cmdApps.set(ShortcutCommand.class, new ShortcutCommandApplication());
    cmdApps.set(ContactCommand.class, new ContactCommandApplication());
    cmdApps.set(GoAroundCommand.class, new GoAroundCommandApplication());
    cmdApps.set(ReportDivertTimeCommand.class, new ReportDivertTimeCommandApplication());
    cmdApps.set(DivertCommand.class, new DivertCommandApplication());
    cmdApps.set(AltitudeRestrictionCommand.class, new AltitudeRestrictionApplication());
    cmdApps.set(ClearedToRouteCommand.class, new ClearedToRouteApplication());

    notApps = new EMap<>();
    notApps.set(RadarContactConfirmationNotification.class, new RadarContactConfirmationNotificationApplication());
  }
}
