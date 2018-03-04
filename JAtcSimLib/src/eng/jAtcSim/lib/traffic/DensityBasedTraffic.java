package eng.jAtcSim.lib.traffic;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.global.ECollections;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.traffic.fleets.CompanyFleet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DensityBasedTraffic extends Traffic {

  public static class CodeWeight {
    public String code;
    public double weight;
  }

  public static class HourBlockMovements implements Comparable<HourBlockMovements> {
    public int hour;
    public int arrivals;
    public int departures;

    @Override
    public int compareTo(HourBlockMovements o) {
      return Integer.compare(hour, o.hour);
    }
  }

  private List<CodeWeight> companies = null; // XML
  private List<CodeWeight> countries = null; // XML
  private List<HourBlockMovements> density;
  private List<Movement> scheduledMovements = new ArrayList<>();
  private Integer lastGeneratedHour = null;
  private double nonCommercialFlightProbability = 0; // XML

  @Override
  public Airplane[] getNewAirplanes() {
    List<Movement> readyMovements = new ArrayList<>();
    for (Movement readyMovement : scheduledMovements) {
      if (readyMovement.getInitTime().isBefore(Acc.now())) {
        readyMovements.add(readyMovement);
      }
    }

    Airplane[] ret = new Airplane[readyMovements.size()];
    for (int i = 0; i < ret.length; i++) {
      Movement m = readyMovements.get(i);
      scheduledMovements.remove(m);
      Airplane a = super.convertMovementToAirplane(m);
      ret[i] = a;
    }
    return ret;
  }

  @Override
  public void generateNewMovementsIfRequired() {
    if (lastGeneratedHour == null || lastGeneratedHour == Acc.now().getHours()) {
      generateNewMovements();
    }
  }

  private void generateNewMovements() {
    if (lastGeneratedHour == null) {
      if (density.size() == 0)
        throw new ERuntimeException("Unable to use generic traffic without density specified.");
      // init things
      Collections.sort(density);
      generateTrafficForHour(Acc.now().getHours());
      lastGeneratedHour = Acc.now().getHours();
    }
    lastGeneratedHour++;
    if (lastGeneratedHour > 23) lastGeneratedHour = 0;
    generateTrafficForHour(lastGeneratedHour);
  }

  private void generateTrafficForHour(int hour) {
    HourBlockMovements hbm = density.get(0);
    int index = 0;
    while (index < density.size()) {
      if (density.get(index).hour <= hour)
        hbm = density.get(index);
      else
        break;
    }
    assert hbm != null;

    for (int i = 0; i < hbm.arrivals; i++) {
      generateNewScheduledMovement(hour, false);
    }
    for (int i = 0; i < hbm.departures; i++) {
      generateNewScheduledMovement(hour, true);
    }

  }

  private void generateNewScheduledMovement(int hour, boolean isDeparture) {
    String prefix;
    boolean isNonCommercial = Acc.rnd().nextDouble() < nonCommercialFlightProbability;
    if (isNonCommercial)
      prefix = getRandomCode(this.countries);
    else
      prefix = getRandomCode(this.companies);

    Callsign cls = super.generateCallsign(prefix, isNonCommercial);
    ETime initTime = new ETime(hour, Acc.rnd().nextInt(0, 59), Acc.rnd().nextInt(0, 59));
    int delay = super.generateDelayMinutes();

    AirplaneType type;
    if (isNonCommercial)
      //TODO here should be some like category probability
      type = Acc.types().getRandom();
    else
      type = getFleets().tryGetByIcao(prefix).tryGetRandom().getAirplaneType();

    Movement m = new Movement(cls, type, initTime, delay, isDeparture);
    this.scheduledMovements.add(m);
  }

//  public AirplaneType getRandomByTraffic(String companyIcao) {
//
//    CompanyFleet fleet = getFleets().tryGetByIcao(companyIcao);
//    if (fleet == null)
//      fleet = getDefaultCompanyFleet();
//
//    String typeName = fleet.tryGetRandomTypeName();
//    if (typeName == null)
//      typeName = DEFAULT_AIRPLANE_TYPE_NAME;
//
//    AirplaneType ret;
//    ret = Acc.sim().getPlaneTypes().tryGetByTypeName(typeName);
//    if (ret == null)
//      ret = Acc.sim().getPlaneTypes().getDefaultType();
//
//    return ret;
//  }

  private String getRandomCode(List<CodeWeight> lst) {
    double sum = ECollections.sum(lst, o-> o.weight);
    double rnd = Acc.rnd().nextDouble(0, sum);
    int index = -1;
    while (rnd > 0) {
      index++;
      rnd -= lst.get(index).weight;
    }
    if (index >= lst.size()) index = lst.size() - 1;
    String ret = lst.get(index).code;
    return ret;
  }
}
