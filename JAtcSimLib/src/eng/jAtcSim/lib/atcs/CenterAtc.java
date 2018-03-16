package eng.jAtcSim.lib.atcs;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeSpeedCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.SetAltitudeRestriction;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterNavaidCommand;
import eng.jAtcSim.lib.world.Navaid;

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

  public int getCtrAcceptDistance() {
    return ctrAcceptDistance;
  }

  public int getCtrNavaidAcceptDistance() {
    return ctrNavaidAcceptDistance;
  }

  @Override
  public void unregisterPlaneUnderControl(Airplane plane) {

  }

  @Override
  public void registerNewPlaneUnderControl(Airplane plane) {
    if (plane.isDeparture()) {
      SpeechList cmds = new SpeechList();

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
          ret = false;
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
  protected void processMessagesFromPlane(Airplane p, SpeechList spchs) {
    // nothing to process
  }

  @Override
  protected Atc getTargetAtcIfPlaneIsReadyToSwitch(Airplane plane) {
    Atc ret;
    if (plane.isArrival() && plane.getAltitude() < this.releaseAltitude)
      ret = Acc.atcApp();
    else
      ret = null;
    return ret;
  }

  private int getDepartureRandomTargetAltitude(Airplane p) {
    //TODO this will not work for VFR and "A"-type planes, for which this is too high
    int ret = Acc.rnd().nextInt(20, p.getType().maxAltitude / 1000);
    ret = ret * 1000;
    return ret;
  }
}
