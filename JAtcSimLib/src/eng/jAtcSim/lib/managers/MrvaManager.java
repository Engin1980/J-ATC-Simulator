package eng.jAtcSim.lib.managers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.utilites.NumberUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.logging.CommonRecorder;
import eng.jAtcSim.lib.global.logging.Recorder;
import eng.jAtcSim.lib.world.Border;
import eng.jAtcSim.lib.world.BorderExactPoint;
import eng.jAtcSim.lib.world.BorderPoint;

public class MrvaManager {

  private IList<XMRVA> mrvas = new EList<>();
  private IMap<Airplane, XMRVA> maps = new EMap<>();
  //TODO remove when bug has been found
  private CommonRecorder tmpRecorder = new CommonRecorder("MRVA manager recorder", "mrvaRecorder.log",
      "-");

  public MrvaManager(IList<Border> mrvas) {
    for (Border mrva : mrvas) {
      assert mrva.getType() == Border.eType.mrva;
      assert mrva.isEnclosed();
      assert mrva.getPoints().isAll(q -> q instanceof BorderExactPoint);

      XMRVA xmrva = new XMRVA(mrva);
      this.mrvas.add(xmrva);
    }
  }

  public void registerPlane(Airplane plane) {
    tmpRecorder.write("%s (%s) registered request", plane.getCallsign().toString(), plane.getSqwk().toString());
    maps.set(plane, null);
  }

  public void unregisterPlane(Airplane plane) {
    tmpRecorder.write("%s (%s) unregister request", plane.getCallsign().toString(), plane.getSqwk().toString());
    maps.remove(plane);
  }

  public void evaluateMrvaFails() {
    for (Airplane airplane : maps.getKeys()) {
      evaluateMrvaFail(airplane);
    }
  }

  private void evaluateMrvaFail(Airplane airplane) {
    if (airplane.getState().is(
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    )) {
      airplane.setMrvaError(false);
      if (maps.get(airplane) != null) maps.set(airplane, null);
    } else {
      XMRVA m = maps.get(airplane);
      boolean findNewOne = false;
      if (m == null && airplane.getVerticalSpeed() <= 0)
        findNewOne = true;
      else if (m != null && m.isIn(airplane.getCoordinate()) == false)
        findNewOne = true;
      if (findNewOne) {
        m = this.mrvas.tryGetFirst(q -> q.isIn(airplane.getCoordinate()));
        maps.set(airplane, m);
      }

      boolean isOutOfAltitude = false;
      if (m != null) isOutOfAltitude = m.isIn(airplane.getAltitude());
      if (isOutOfAltitude && airplane.getState().is(Airplane.State.arrivingLow, Airplane.State.departingLow)) {
        // this is for departures/goarounds when close to runway, very low, so are omitted
        double d = Coordinates.getDistanceInNM(airplane.getCoordinate(), Acc.airport().getLocation());
        if (d < 3)
          isOutOfAltitude = false;
      }
      airplane.setMrvaError(isOutOfAltitude);
    }
  }
}

class XMRVA {
  private final double min;
  private final double max;
  private final double globalMinLng;
  private final double globalMaxLng;
  private final double globalMinLat;
  private final double globalMaxLat;
  private final IList<XLine> lines = new EList<>();

  public XMRVA(Border mrva) {
    this.min = mrva.getMinAltitude();
    this.max = mrva.getMaxAltitude();
    BorderExactPoint prev = (BorderExactPoint) mrva.getPoints().getLast(q -> true);

    for (BorderPoint tmp : mrva.getPoints()) {
      BorderExactPoint curr = (BorderExactPoint) tmp;
      XLine line = new XLine(prev.getCoordinate(), curr.getCoordinate());
      lines.add(line);
      prev = curr;
    }

    this.globalMinLng = lines.min(q -> q.a.getLongitude().get());
    this.globalMaxLng = lines.max(q -> q.b.getLongitude().get());
    this.globalMinLat = lines.min(q -> Math.min(q.a.getLatitude().get(), q.b.getLatitude().get()));
    this.globalMaxLat = lines.max(q -> Math.max(q.a.getLatitude().get(), q.b.getLatitude().get()));
  }

  public boolean isIn(Coordinate c) {
    boolean ret = NumberUtils.isBetweenOrEqual(globalMinLng, c.getLongitude().get(), globalMaxLng);
    if (ret)
      ret = NumberUtils.isBetweenOrEqual(globalMinLat, c.getLatitude().get(), globalMaxLat);
    if (ret) {
      int hit = 0;
      for (XLine line : lines) {
        if (line.b.getLongitude().get() < c.getLongitude().get()) {
          // line longitude on the left side
          continue;
        } else if (line.a.getLongitude().get() > c.getLongitude().get()) {
          // line longitude on the right side
          double latMin = line.a.getLatitude().get();
          double latMax = line.b.getLatitude().get();
          if (latMin > latMax) {
            double tmp = latMin;
            latMin = latMax;
            latMax = tmp;
          }
          if (NumberUtils.isBetweenOrEqual(latMin, c.getLatitude().get(), latMax)) hit++;
        } else {
          // line longitude in range
          if (!NumberUtils.isInRange(line.a.getLatitude().get(), c.getLatitude().get(), line.b.getLatitude().get()))
            continue;
          double a = (line.b.getLatitude().get() - line.a.getLatitude().get()) / (line.b.getLongitude().get() - line.a.getLongitude().get());
          double b = line.a.getLatitude().get() - a * line.a.getLongitude().get();
          double p = a * c.getLongitude().get() + b;
          double diff = c.getLatitude().get() - p;
          if (a >= 0 && diff > 0)
            hit++;
          else if (a < 0 && diff < 0)
            hit++;
        }
      }
      ret = (hit % 2 == 1);
    }

    return ret;
  }

  public boolean isIn(double altitude) {
    boolean ret = NumberUtils.isBetween(this.min, altitude, this.max);
    return ret;
  }
}

class XLine {
  public final Coordinate a;
  public final Coordinate b;

  public XLine(Coordinate a, Coordinate b) {
    if (a.getLongitude().get() > b.getLongitude().get()) {
      this.a = b;
      this.b = a;
    } else {
      this.a = a;
      this.b = b;
    }
  }
}
