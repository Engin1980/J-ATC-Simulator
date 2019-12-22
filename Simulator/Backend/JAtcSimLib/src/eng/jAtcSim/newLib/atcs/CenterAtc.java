package eng.jAtcSim.newLib.area.atcs;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplane4Atc;
import eng.jAtcSim.newLib.area.atcs.planeResponsibility.SwitchRoutingRequest;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.StringMessageContent;
import eng.jAtcSim.newLib.area.speaking.SpeechList;
import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.newLib.area.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.*;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.afters.AfterNavaidCommand;
import eng.jAtcSim.newLib.area.serialization.LoadSave;
import eng.jAtcSim.newLib.world.Navaid;
import eng.jAtcSim.newLib.world.DARoute;
import eng.jAtcSim.newLib.world.ActiveRunwayThreshold;

public class CenterAtc extends ComputerAtc {

  private int ctrAcceptDistance = 40;
  private int ctrNavaidAcceptDistance = 15;

  private IList<IAirplane4Atc> farArrivals = new EList<>();
  private IList<IAirplane4Atc> middleArrivals = new EList<>();
  private IList<IAirplane4Atc> closeArrivals = new EList<>();

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
  public void elapseSecond() {
    super.elapseSecond();

    if (Acc.now().getTotalSeconds() % 16 == 0) {
      double dist;

      IList<IAirplane4Atc> tmp = new EList<>();

      for (IAirplane4Atc plane : middleArrivals) {
        try {
          evaluateMiddleArrivalsForCloseArrivals(tmp, plane);
        } catch (Exception ex) {
          throw new EApplicationException("Failed to evaluate " + plane.getFlightModule().getCallsign() + ".", ex);
        }
      }
      middleArrivals.remove(tmp);
      closeArrivals.add(tmp);
      tmp.clear();

      for (IAirplane4Atc plane : farArrivals) {
        dist = Coordinates.getDistanceInNM(plane.getRoutingModule().getEntryExitPoint().getCoordinate(), plane.getCoordinate());
        if (dist < 50) {
          if (plane.getSha().getAltitude() > 29_000) {
            int newAlt = Acc.rnd().nextInt(25, 29) * 1_000;
            SpeechList sl = new SpeechList();
            sl.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, newAlt));
            Message m = new Message(this, plane, sl);
            super.sendMessage(m);
          }
          tmp.add(plane);
        }
      }
      farArrivals.remove(tmp);
      middleArrivals.add(tmp);
      tmp.clear();
    }
  }

  @Override
  public void unregisterPlaneUnderControl(IAirplane4Atc plane) {
    if (plane.getFlightModule().isArrival()) {
      farArrivals.tryRemove(plane);
      middleArrivals.tryRemove(plane);
      closeArrivals.tryRemove(plane);
    }
  }

  @Override
  public void removePlaneDeletedFromGame(IAirplane4Atc plane) {

  }

  @Override
  public void registerNewPlaneUnderControl(IAirplane4Atc plane, boolean finalRegistration) {
    if (plane.getFlightModule().isArrival())
      farArrivals.add(plane);
  }

  @Override
  protected boolean acceptsNewRouting(IAirplane4Atc plane, SwitchRoutingRequest srr) {
    boolean ret;
    ret = srr.route.isValidForCategory(plane.getType().category)
        && (srr.route.getType() == DARoute.eType.vectoring
        || srr.route.getType() == DARoute.eType.star
        || srr.route.getType() == DARoute.eType.transition);
    return ret;
  }

  private void evaluateMiddleArrivalsForCloseArrivals(IList<IAirplane4Atc> tmp, IAirplane4Atc plane) {
    double dist;
    dist = Coordinates.getDistanceInNM(plane.getRoutingModule().getEntryExitPoint().getCoordinate(), plane.getCoordinate());
    if (dist < 27) {
      SpeechList<IAtcCommand> cmds = new SpeechList<>();
      if (plane.getSha().getTargetAltitude() > Acc.atcCtr().getOrderedAltitude())
        cmds.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, Acc.atcCtr().getOrderedAltitude()));

      // assigns route
      Navaid n = plane.getRoutingModule().getEntryExitPoint();
      Tuple<DARoute, ActiveRunwayThreshold> rrt = getRoutingForPlaneAndFix(plane, n);
      DARoute r = rrt.getA();
      ActiveRunwayThreshold rt = rrt.getB();
      cmds.add(new ProceedDirectCommand(n));
      cmds.add(new ClearedToRouteCommand(r, rt));
      Message msg = new Message(this, plane, cmds);
      super.sendMessage(msg);

      tmp.add(plane);
    }
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
  protected boolean shouldBeSwitched(IAirplane4Atc plane) {
    return true;
  }

  @Override
  protected RequestResult canIAcceptPlane(IAirplane4Atc p) {
    RequestResult ret;
    if (p.getFlightModule().isArrival()) {
      ret = new RequestResult(false, String.format("%s is an arrival.", p.getFlightModule().getCallsign().toString()));
    } else {
      if (p.getRoutingModule().isOnWayToPassDeparturePoint() == false) {
        ret = new RequestResult(false,
            String.format("%s is not heading (or on the route to) departure fix %s",
                p.getFlightModule().getCallsign().toString(),
                p.getRoutingModule().getAssignedRoute().getMainNavaid().getName()));
      } else {
        if (p.getSha().getAltitude() > super.acceptAltitude || p.getSha().getAltitude() > (p.getType().maxAltitude * .666)) {
          ret = new RequestResult(true, null);
        } else {
          double aipDist = Coordinates.getDistanceInNM(p.getCoordinate(), Acc.airport().getLocation());
          if (aipDist > this.ctrAcceptDistance) {
            ret = new RequestResult(true, null);
          } else {
            double navDist = Coordinates.getDistanceInNM(p.getCoordinate(), p.getRoutingModule().getDepartureLastNavaid().getCoordinate());
            if (navDist < this.ctrNavaidAcceptDistance) {
              ret = new RequestResult(true, null);
            } else {
              ret = new RequestResult(false, String.format(
                  "%s is too far from departure fix %s, or not enough far from airport, or not enough high.",
                  p.getFlightModule().getCallsign().toString(),
                  p.getRoutingModule().getAssignedRoute().getName()
              ));
            }
          }
        }
      }
    }
    return ret;
  }

  @Override
  protected void processMessagesFromPlane(IAirplane4Atc plane, SpeechList spchs) {
    for (Object o : spchs) {
      if (o instanceof GoodDayNotification) {
        if (((GoodDayNotification) o).isRepeated()) continue; // repeated g-d-n are ignored
        if (plane.getFlightModule().isDeparture() && Acc.prm().getResponsibleAtc(plane) == this) {
          SpeechList cmds = new SpeechList();

          cmds.add(
              new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.climb, getDepartureRandomTargetAltitude(plane)));
          cmds.add(
              new SetAltitudeRestriction(null)); // to abort altitude restriction
          cmds.add(
              new ChangeSpeedCommand()); // to abort speeed restriction

          // order to continue after last fix
          Navaid n = plane.getRoutingModule().getDepartureLastNavaid();
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
  protected Atc getTargetAtcIfPlaneIsReadyToSwitch(IAirplane4Atc plane) {
    Atc ret;
    if (plane.getFlightModule().isArrival()) {
      if (plane.getEmergencyModule().isEmergency())
        ret = Acc.atcApp();
      else {
        if (closeArrivals.contains(plane) == false) {
          return null;
        }
        Navaid n = plane.getRoutingModule().getEntryExitPoint();
        double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), n.getCoordinate());
        if (dist <= 10) {
          ret = Acc.atcApp();
        } else
          ret = null;
      }
    } else
      ret = null;
    return ret;
  }

  @Override
  protected void _save(XElement elm) {
    super._save(elm);
    LoadSave.saveField(elm, this, "farArrivals");
    LoadSave.saveField(elm, this, "middleArrivals");
    LoadSave.saveField(elm, this, "closeArrivals");
  }

  @Override
  protected void _load(XElement elm) {
    super._load(elm);
    LoadSave.loadField(elm, this, "farArrivals");
    LoadSave.loadField(elm, this, "middleArrivals");
    LoadSave.loadField(elm, this, "closeArrivals");
  }

  private Tuple<DARoute, ActiveRunwayThreshold> getRoutingForPlaneAndFix(IAirplane4Atc plane, Navaid n) {
    Tuple<DARoute, ActiveRunwayThreshold> ret;
    DARoute r = null;
    ActiveRunwayThreshold rt = null;
    IList<ActiveRunwayThreshold> thresholdsCopy = new EList<>();

    IReadOnlyList<ActiveRunwayThreshold> thresholds;
    // if is arrival, scheduled thresholds are taken into account
    if (Acc.atcTwr().tryGetRunwayConfigurationScheduled() != null)
      thresholds = Acc.atcTwr().tryGetRunwayConfigurationScheduled().getArrivals()
          .where(q -> q.isForCategory(plane.getType().category))
          .select(q -> q.getThreshold());
    else
      thresholds = Acc.atcTwr().getRunwayConfigurationInUse().getArrivals()
          .where(q -> q.isForCategory(plane.getType().category))
          .select(q -> q.getThreshold());

    thresholdsCopy = new EList<>(thresholds);
    while (r == null && !thresholdsCopy.isEmpty()) {
      rt = thresholdsCopy.getRandom();
      thresholdsCopy.remove(rt);
      r = rt.getArrivalRouteForPlane(plane.getType(), plane.getSha().getTargetAltitude(), plane.getRoutingModule().getEntryExitPoint(), false);
    }
    if (thresholdsCopy.isEmpty() && r == null) {
      rt = thresholds.getRandom();
      r = rt.getArrivalRouteForPlane(plane.getType(), plane.getSha().getTargetAltitude(), plane.getRoutingModule().getEntryExitPoint(), true);
    }
    assert rt != null;
    assert r != null;

    ret = new Tuple<>(r, rt);
    return ret;
  }

  private int getDepartureRandomTargetAltitude(IAirplane4Atc p) {
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
    min = Math.max(p.getSha().getAltitude() / 1000, min);
    int ret = Acc.rnd().nextInt(min, p.getType().maxAltitude / 1000);
    ret = ret * 1000;
    return ret;
  }
}
