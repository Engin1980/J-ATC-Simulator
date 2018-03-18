package eng.jAtcSim.lib.atcs;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeSpeedCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.SetAltitudeRestriction;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterNavaidCommand;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;

public class CenterAtc extends ComputerAtc {

  private int ctrAcceptDistance = 40;
  private int ctrNavaidAcceptDistance = 15;

  public CenterAtc(AtcTemplate template) {
    super(template);
    if (template.getCtrAcceptDistance() != null)
      this.ctrAcceptDistance = template.getCtrAcceptDistance();
    if (template.getCtrNavaidAcceptDistance() != null)
      this.ctrNavaidAcceptDistance = template.getCtrNavaidAcceptDistance();
  }

  @Override
  protected void processMessageFromAtc(Message m) {
    // do nothing , CTR has no messages acceptable from ATC
    super.sendMessage(new Message(
        this,
        m.getSource(),
        new StringMessageContent("Unable.")
    ));
  }

  public int getCtrAcceptDistance() {
    return ctrAcceptDistance;
  }

  public int getCtrNavaidAcceptDistance() {
    return ctrNavaidAcceptDistance;
  }

  @Override
  public void unregisterPlaneUnderControl(Airplane plane, boolean finalUnregistration) {

  }

  @Override
  public void registerNewPlaneUnderControl(Airplane plane, boolean finalRegistration) {

  }

  @Override
  protected boolean shouldBeSwitched(Airplane plane) {
    return true;
  }

  @Override
  protected boolean canIAcceptPlane(Airplane p) {
    boolean ret;
    if (p.isArrival()) {
      ret = false;
    } else {
      if (p.isOnWayToPassDeparturePoint() == false) {
        ret = false;
      } else {
        if (p.getAltitude() > super.acceptAltitude) {
          ret = true;
        } else {
          double aipDist = Coordinates.getDistanceInNM(p.getCoordinate(), Acc.airport().getLocation());
          if (aipDist > this.ctrAcceptDistance) {
            ret = true;
          } else {
            double navDist = Coordinates.getDistanceInNM(p.getCoordinate(), p.getDepartureLastNavaid().getCoordinate());
            if (navDist < this.ctrNavaidAcceptDistance) {
              ret = true;
            } else {
              ret = false;
            }
          }
        }
      }
    }
    return ret;
  }

  @Override
  protected void processMessagesFromPlane(Airplane plane, SpeechList spchs) {
    for (Object o : spchs) {
      if (o instanceof GoodDayNotification) {
        if (plane.isDeparture()) {
          SpeechList cmds = new SpeechList();

          cmds.add(
              new RadarContactConfirmationNotification());
          cmds.add(
              new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.climb, getDepartureRandomTargetAltitude(plane)));
          cmds.add(
              new SetAltitudeRestriction(null)); // to abort altitude restriction
          cmds.add(
              new ChangeSpeedCommand()); // to abort speeed restriction

          // order to continue after last fix
          Navaid n = plane.getDepartureLastNavaid();
          cmds.add(new AfterNavaidCommand(n));
          cmds.add(new ChangeHeadingCommand());

          Message m = new Message(this, plane, cmds);
          super.sendMessage(m);
        }
      }

    }
    // nothing to process
  }

  @Override
  protected Atc getTargetAtcIfPlaneIsReadyToSwitch(Airplane plane) {
    Atc ret = null;
    if (plane.isArrival()) {
      if (plane.getAltitude() <= this.releaseAltitude) {
        ret = Acc.atcApp();
      } else {
        Route r = plane.getAssigneRoute();
        Navaid n = r.getMainFix();
        double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), n.getCoordinate());
        if (dist < 15) {
          ret = Acc.atcApp();
        }
      }
    }
    return ret;
  }

  private int getDepartureRandomTargetAltitude(Airplane p) {
    //TODO this will not work for VFR and "A"-type planes, for which this is too high
    int ret = Acc.rnd().nextInt(20, p.getType().maxAltitude / 1000);
    ret = ret * 1000;
    return ret;
  }
}
