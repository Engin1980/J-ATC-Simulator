package eng.jAtcSim.lib.atcs;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterNavaidCommand;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.RunwayThreshold;

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
  public void unregisterPlaneUnderControl(Airplane plane, boolean finalUnregistration) {
  }

  @Override
  public void registerNewPlaneUnderControl(Airplane plane, boolean finalRegistration) {
    if (finalRegistration) {
      SpeechList<IAtcCommand> cmds = new SpeechList<>();
      cmds.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, Acc.atcCtr().getOrderedAltitude()));

      // assigns route
      Navaid n = plane.getEntryExitFix();
      Route r = getRouteForPlaneAndFix(plane, n);
      cmds.add(new ProceedDirectCommand(n));
//      cmds.add(new ThenCommand());
      cmds.add(new ClearedToRouteCommand(r));
      Message msg = new Message(this, plane, cmds);
      super.sendMessage(msg);
    }
  }

  private Route getRouteForPlaneAndFix(Airplane plane, Navaid n) {

    IList<Route> rts = new EList<>();

    IList<RunwayThreshold> thresholds;
    // if is arrival, scheduled thresholds are taken into account
    thresholds = Acc.atcTwr().getRunwayThresholdsScheduled();
    if (thresholds.isEmpty())
      thresholds = Acc.thresholds();

    for (RunwayThreshold threshold : thresholds) {
      rts.add(threshold.getRoutes());
    }
    rts = rts.where(q -> q.getType() != Route.eType.sid);

    rts = rts.where(q -> q.getMaxMrvaAltitude() < plane.getType().maxAltitude);

    rts = rts.where(q -> q.isValidForCategory(plane.getType().category));

    assert !rts.isEmpty() : "Here should be at least one route, otherwise the arrival had to be canceled during creationg for NO-IFR route";

    Route ret;
      ret = rts.getRandom();

    return ret;
  }

  @Override
  protected void processNonPlaneSwitchMessageFromAtc(Message m) {
    // do nothing , ctr has no messages acceptable from ATC
    super.sendMessage(new Message(
        this,
        m.getSource(),
        new StringMessageContent("Unable.")
    ));
  }

  @Override
  protected boolean shouldBeSwitched(Airplane plane) {
    return true;
  }

  @Override
  protected RequestResult canIAcceptPlane(Airplane p) {
    RequestResult ret;
    if (p.isArrival()) {
      ret = new RequestResult(false, String.format("%s is an arrival.", p.getCallsign().toString()));
    } else {
      if (p.isOnWayToPassDeparturePoint() == false) {
        ret = new RequestResult(false,
            String.format("%s is not heading (or on the route to) departure fix %s",
                p.getCallsign().toString(),
                p.getAssigneRoute().getMainFix().getName()));
      } else {
        if (p.getAltitude() > super.acceptAltitude || p.getAltitude() > (p.getType().maxAltitude * .666)) {
          ret = new RequestResult(true, null);
        } else {
          double aipDist = Coordinates.getDistanceInNM(p.getCoordinate(), Acc.airport().getLocation());
          if (aipDist > this.ctrAcceptDistance) {
            ret = new RequestResult(true, null);
          } else {
            double navDist = Coordinates.getDistanceInNM(p.getCoordinate(), p.getDepartureLastNavaid().getCoordinate());
            if (navDist < this.ctrNavaidAcceptDistance) {
              ret = new RequestResult(true, null);
            } else {
              ret = new RequestResult(false, String.format(
                  "%s is too far from departure fix %s, or not enough far from airport, or not enough high.",
                  p.getCallsign().toString(),
                  p.getAssigneRoute().getName()
              ));
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
      if (plane.isEmergency())
        ret = Acc.atcApp();
      else {
        Navaid n = plane.getEntryExitFix();
        double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), n.getCoordinate());
        if (dist <= 10) {
          ret = Acc.atcApp();
        }
      }
    }
    return ret;
  }

  @Override
  protected void _save(XElement elm) {
    super._save(elm);
  }

  @Override
  protected void _load(XElement elm) {
    super._load(elm);
  }

  private int getDepartureRandomTargetAltitude(Airplane p) {
    int min;
    switch (p.getType().category) {
      case 'A':
        min = 4;
        break;
      case 'B':
        min = 14;
        break;
      case 'C':
      case 'D':
        min = 20;
        break;
      default:
        throw new UnsupportedOperationException();
    }
    min = (int) Math.max(p.getAltitude() / 1000, min);
    int ret = Acc.rnd().nextInt(min, p.getType().maxAltitude / 1000);
    ret = ret * 1000;
    return ret;
  }
}
