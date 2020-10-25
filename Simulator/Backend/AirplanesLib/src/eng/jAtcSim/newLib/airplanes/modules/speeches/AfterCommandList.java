package eng.jAtcSim.newLib.airplanes.modules.speeches;

import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.utilites.ConversionUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.InternalAcc;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.*;

import java.util.function.Predicate;

public class AfterCommandList {

  public enum Type {
    route,
    extensions
  }

  private static boolean isLateralDirectionAfterNavaidCommand(Coordinate coordinate, AFItem item) {
    boolean ret = false;
    if (item.antecedent instanceof AfterDistanceCommand) {
      AfterDistanceCommand anc = (AfterDistanceCommand) item.antecedent;
      Navaid navaid = Context.getArea().getNavaids().get(anc.getNavaidName());
      if (navaid.getCoordinate().equals(coordinate)) {
        if (item.consequent instanceof ChangeHeadingCommand ||
                item.consequent instanceof ProceedDirectCommand ||
                item.consequent instanceof HoldCommand ||
                item.consequent instanceof ShortcutCommand ||
                item.consequent instanceof ClearedToApproachCommand)
          ret = true;
      }
    }
    return ret;
  }

  private static boolean isAFItemPassed(AFItem item, IAirplane plane, Coordinate currentTargetCoordinateOrNull) {
    boolean ret;

    if (item.antecedent instanceof AfterImmediatelyCommand) {
      ret = true;
    } else if (item.antecedent instanceof AfterAltitudeCommand) {
      AfterAltitudeCommand cmd = (AfterAltitudeCommand) item.antecedent;
      ret = isAfterAltitudePassed(cmd, plane.getSha().getAltitude());
    } else if (item.antecedent instanceof AfterSpeedCommand) {
      int trgSpd = ((AfterSpeedCommand) item.antecedent).getSpeed();
      ret = (Math.abs(trgSpd - plane.getSha().getSpeed()) < 10);
    } else if (item.antecedent instanceof AfterNavaidCommand) {
      AfterNavaidCommand anc = (AfterNavaidCommand) item.antecedent;
      Navaid navaid = Context.getArea().getNavaids().get(anc.getNavaidName());
      if ((navaid.getCoordinate().equals(currentTargetCoordinateOrNull) == false)) {
        // flying over some navaid, but not over current targeted by plane(pilot)
        ret = false;
      } else {
        double dist = Coordinates.getDistanceInNM(
                navaid.getCoordinate(),
                plane.getCoordinate());
        double overDist = Navaid.getOverNavaidDistance(plane.getSha().getSpeed());
        ret = (dist < overDist);
      }
    } else if (item.antecedent instanceof AfterHeadingCommand) {
      AfterHeadingCommand anc = (AfterHeadingCommand) item.antecedent;
      double trgHdg = Headings.add(anc.getHeading(), Context.getArea().getAirport().getDeclination());
      double diff = Headings.getDifference(plane.getSha().getHeading(), trgHdg, true);
      ret = (diff < 3);
    } else if (item.antecedent instanceof AfterDistanceCommand) {
      AfterDistanceCommand anc = (AfterDistanceCommand) item.antecedent;
      Navaid navaid = Context.getArea().getNavaids().get(anc.getNavaidName());
      double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), navaid.getCoordinate());
      switch (anc.getPosition()) {
        case above:
          ret = dist - anc.getDistance() > -0.3;
          break;
        case below:
          ret = anc.getDistance() - dist > -0.3;
          break;
        case exactly:
          ret = Math.abs((anc.getDistance() - dist)) < 0.3;
          break;
        default:
          throw new EEnumValueUnsupportedException(anc.getPosition());
      }
    } else if (item.antecedent instanceof AfterRadialCommand) {
      AfterRadialCommand anc = (AfterRadialCommand) item.antecedent;
      Navaid navaid = Context.getArea().getNavaids().get(anc.getNavaidName());
      double rad = Coordinates.getBearing(navaid.getCoordinate(), plane.getCoordinate());
      double diff = Headings.getDifference(rad, anc.getRadial(), true);
      ret = (diff < 3);
    } else {
      throw new UnsupportedOperationException();
    }
    return ret;
  }

  private static SpeechList<ICommand> getAndRemoveSatisfiedCommands(
          IList<AFItem> lst,
          IAirplane referencePlane, Coordinate currentTargetCoordinateOrNull,
          boolean untilFirstNotSatisfied) {

    SpeechList<ICommand> ret = new SpeechList<>();

    int i = 0;
    while (i < lst.size()) {
      AFItem item = lst.get(i);
      if (isAFItemPassed(item, referencePlane, currentTargetCoordinateOrNull)) {
        ret.add(item.consequent);
        lst.removeAt(i);
      } else {
        if (untilFirstNotSatisfied)
          break;
        else
          i++;
      }
    }

    return ret;
  }

  private static boolean isAfterAltitudePassed(AfterAltitudeCommand cmd, double altitudeInFt) {
    int trgAlt = cmd.getAltitude();
    boolean ret;
    switch (cmd.getPosition()) {
      case exactly:
        ret = Math.abs(trgAlt - altitudeInFt) < 100;
        break;
      case above:
        ret = altitudeInFt > trgAlt;
        break;
      case below:
        ret = altitudeInFt < trgAlt;
        break;
      default:
        throw new UnsupportedOperationException();
    }

    return ret;
  }

  private static void clearChangeSpeedClass(IList<AFItem> lst, int referenceSpeed, boolean isArrival) {
    IList<AFItem> tmp = lst.where(q -> q.consequent instanceof ChangeSpeedCommand);
    Predicate<AFItem> prd;
    if (isArrival)
      prd = afItem -> {
        ChangeSpeedCommand c = (ChangeSpeedCommand) afItem.consequent;
        boolean ret = c.getRestriction().value >= referenceSpeed;
        return ret;
      };
    else
      prd = afItem -> {
        ChangeSpeedCommand c = (ChangeSpeedCommand) afItem.consequent;
        boolean ret = c.getRestriction().value <= referenceSpeed;
        return ret;
      };
    tmp.retain(prd);
    lst.removeMany(tmp);
  }

  private static boolean hasProceedDirectToNavaidAsConseqent(IList<AFItem> items, Navaid navaid) {
    Predicate<AFItem> cond = q -> {
      boolean ret =
              (q.consequent instanceof ToNavaidCommand &&
                      InternalAcc.getNavaid((ToNavaidCommand) q.consequent).equals(navaid));
      return ret;
    };
    boolean ret = items.isAny(q -> cond.test(q));
    return ret;
  }

  private final IList<AFItem> rt = new EList<>();
  private final IList<AFItem> ex = new EList<>();

  public void addExtension(AfterCommand afterCommand, ICommand consequent) {
    AFItem it = new AFItem(afterCommand, consequent);
    ex.add(it);
  }

  public void addRoute(AfterCommand afterCommand, ICommand consequent) {
    AFItem it = new AFItem(afterCommand, consequent);
    this.rt.add(it);
  }

  public void clearAll() {
    this.rt.clear();
    this.ex.clear();
  }

  public boolean clearAllAltitudeRestrictions() {
    boolean ret = this.rt.isAny(q -> q.consequent instanceof AltitudeRestrictionCommand);
    this.rt.remove(q -> q.consequent instanceof AltitudeRestrictionCommand);
    return ret;
  }

  public void clearChangeAltitudeClass(int referenceAltitudeInFt, boolean isArrival) {
    IList<AFItem> tmp = this.ex.where(q -> q.consequent instanceof ChangeAltitudeCommand);
    Predicate<AFItem> prd;
    if (isArrival)
      prd = afItem -> {
        ChangeAltitudeCommand c = (ChangeAltitudeCommand) afItem.consequent;
        boolean ret = c.getAltitudeInFt() >= referenceAltitudeInFt;
        return ret;
      };
    else
      prd = afItem -> {
        ChangeAltitudeCommand c = (ChangeAltitudeCommand) afItem.consequent;
        boolean ret = c.getAltitudeInFt() <= referenceAltitudeInFt;
        return ret;
      };
    tmp.retain(prd);
    this.ex.removeMany(tmp);
  }

  public void clearChangeSpeedClass(int referenceSpeed, boolean isArrival, Type type) {
    IList<AFItem> tmp = (type == Type.route) ? this.rt : this.ex;
    clearChangeSpeedClass(tmp, referenceSpeed, isArrival);
  }

  public void clearChangeSpeedClassOfRouteWithTransferConsequent(Integer referenceSpeedOrNull, boolean isArrival) {
    int i = 1;
    while (i < this.rt.size()) {
      AFItem it = this.rt.get(i);
      i++;
      if ((it.antecedent instanceof AfterSpeedCommand) == false) continue;
      AfterSpeedCommand tmp = (AfterSpeedCommand) it.antecedent;
      if (referenceSpeedOrNull != null && (isArrival && tmp.getSpeed() < referenceSpeedOrNull)) continue;
      if (referenceSpeedOrNull != null && (!isArrival && tmp.getSpeed() > referenceSpeedOrNull)) continue;

      i--;
      AfterCommand a = this.rt.get(i - 1).antecedent;
      ICommand c = this.rt.get(i).consequent;

      AFItem af = new AFItem(a, c);
      this.rt.add(af);
      this.rt.remove(it);
    }
  }

  public void clearExtensionsByConsequent(Class<?>[] cmdTypes) {
    this.ex.remove(q -> ConversionUtils.isInstanceOf(q.consequent, cmdTypes));
  }

  public void clearExtensionsByConsequent(Class<?> cmdType) {
    Class<?>[] tmp = new Class[]{cmdType};
    this.clearExtensionsByConsequent(tmp);
  }

  public void clearRoute() {
    this.rt.clear();
  }

  public SpeechList<ICommand> doShortcutTo(Navaid n) {
    SpeechList<ICommand> ret;
    if (hasProceedDirectToNavaidAsConseqent(this.rt, n))
      ret = doShortcutTo(this.rt, n);
    else if (hasProceedDirectToNavaidAsConseqent(this.ex, n))
      ret = doShortcutTo(this.ex, n);
    else
      throw new UnsupportedOperationException();
    return ret;
  }

  public SpeechList<ICommand> getAndRemoveSatisfiedCommands(
          IAirplane referencePlane, Coordinate currentTargetCoordinateOrNull, Type type) {
    SpeechList<ICommand> ret;
    IList<AFItem> tmp;
    boolean untilFirstNotSatisfied;
    switch (type) {
      case extensions:
        tmp = this.ex;
        untilFirstNotSatisfied = false;
        break;
      case route:
        tmp = this.rt;
        untilFirstNotSatisfied = true;
        break;
      default:
        throw new UnsupportedOperationException();
    }

    ret = getAndRemoveSatisfiedCommands(tmp, referencePlane, currentTargetCoordinateOrNull, untilFirstNotSatisfied);

    return ret;
  }

  public IReadOnlyList<Tuple<AfterCommand, ICommand>> getAsList(Type type) {
    IList<Tuple<AfterCommand, ICommand>> ret;
    IList<AFItem> tmp = type == Type.route ? this.rt : this.ex;
    ret = tmp.select(q -> new Tuple<>(q.antecedent, q.consequent));
    return ret;
  }

  public boolean hasLateralDirectionAfterCoordinate(Coordinate coordinate) {
    boolean ret =
            this.rt.isAny(q -> isLateralDirectionAfterNavaidCommand(coordinate, q))
                    ||
                    this.ex.isAny(q -> isLateralDirectionAfterNavaidCommand(coordinate, q));
    return ret;
  }

  public boolean hasProceedDirectToNavaidAsConseqent(Navaid navaid) {
//    Predicate<AFItem> cond = q -> {
//      boolean ret =
//          (q.consequent instanceof ToNavaidCommand &&
//              InternalAcc.getNavaid((ToNavaidCommand) q.consequent).equals(navaid));
//      return ret;
//    };
    boolean ret = hasProceedDirectToNavaidAsConseqent(this.rt, navaid) || hasProceedDirectToNavaidAsConseqent(this.ex, navaid);
    return ret;
  }

  public boolean isRouteEmpty() {
    return this.rt.isEmpty();
  }

  public String toLogString() {
    EStringBuilder sb = new EStringBuilder();

    for (AFItem afItem : rt) {
      sb.appendFormat("--route--:: %s\n", afItem.toString());
    }

    for (AFItem afItem : ex) {
      sb.appendFormat("--exatc--:: %s\n", afItem.toString());
    }

    return sb.toString();
  }

  private SpeechList<ICommand> doShortcutTo(IList<AFItem> lst, Navaid n) {
    SpeechList<ICommand> ret = new SpeechList<>();

    while (lst.size() > 0) {
      AFItem it = lst.get(0);
      lst.removeAt(0);
      ret.add(it.consequent);
      if ((it.consequent instanceof ToNavaidCommand) && InternalAcc.getNavaid((ToNavaidCommand) it.consequent).equals(n))
        break;
    }

    return ret;
  }
}

class AFItem {
  public final AfterCommand antecedent;
  public final ICommand consequent;

  public AFItem(AfterCommand antecedent, ICommand consequent) {
    this.antecedent = antecedent;
    this.consequent = consequent;
  }

  @Override
  public String toString() {
    return String.format("%s ==> %s", antecedent, consequent);
  }
}
