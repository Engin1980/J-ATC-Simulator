package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterCommand;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;

import java.util.HashMap;
import java.util.Map;

public class ApplicationManager {
  private static Map<Class, CommandApplication> cmdApps;
  private static Map<Class, NotificationApplication> notApps;

  public static ConfirmationResult confirm(IAirplaneWriteSimple plane, IFromAtc c, boolean checkStateSanity, boolean checkCommandSanity) {
    ConfirmationResult ret;

    if (c instanceof AfterCommand) {
      ret = new ConfirmationResult();
      ret.confirmation = new Confirmation((AfterCommand) c);
    } else if (c instanceof IAtcCommand) {
      CommandApplication ca = cmdApps.get(c.getClass());
      assert ca != null : "Unknown application. Probably not added into cmdApps list?";
      assert plane != null;
      assert c != null;
      ret = ca.confirm(plane, (IAtcCommand) c, checkStateSanity, checkCommandSanity);
    } else if (c instanceof IAtcNotification) {
      NotificationApplication na = notApps.get(c.getClass());
      assert na != null;
      ret = na.confirm(plane, (IAtcNotification) c);
    } else {
      throw new UnsupportedOperationException();
    }

    return ret;

  }

  public static ApplicationResult apply(IAirplaneWriteSimple plane, IFromAtc c) {
    ApplicationResult ret;

    if (c instanceof IAtcCommand) {
      ret = cmdApps.get(c.getClass()).apply(plane, (IAtcCommand) c, false);
    } else if (c instanceof IAtcNotification) {
      ret = notApps.get(c.getClass()).apply(plane, (IAtcNotification) c);
    } else {
      throw new UnsupportedOperationException();
    }

    return ret;
  }

  static {
    cmdApps = new HashMap<>();

    cmdApps.put(ChangeAltitudeCommand.class, new ChangeAltitudeApplication());
    cmdApps.put(ChangeHeadingCommand.class, new ChangeHeadingApplication());
    cmdApps.put(ChangeSpeedCommand.class, new ChangeSpeedApplication());
    cmdApps.put(ClearedForTakeoffCommand.class, new ClearedForTakeOffCommandApplication());
    cmdApps.put(ClearedToApproachCommand.class, new ClearedToApproachApplication());
    cmdApps.put(HoldCommand.class, new HoldCommandApplication());
    cmdApps.put(ProceedDirectCommand.class, new ProceedDirectApplication());
    cmdApps.put(ShortcutCommand.class, new ShortcutCommandApplication());
    cmdApps.put(ContactCommand.class, new ContactCommandApplication());
    cmdApps.put(GoAroundCommand.class, new GoAroundCommandApplication());
    cmdApps.put(ReportDivertTime.class, new ReportDivertTimeCommandApplication());
    cmdApps.put(DivertCommand.class, new DivertCommandApplication());
    cmdApps.put(SetAltitudeRestriction.class, new SetAltitudeRestrictionApplication());
    cmdApps.put(ClearedToRouteCommand.class, new ClearedToRouteApplication());

    notApps = new HashMap<>();
    notApps.put(RadarContactConfirmationNotification.class, new RadarContactConfirmationNotificationApplication());
  }
}
