package eng.jAtcSim.lib.traffic;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.eSystem.utilites.CollectionUtils;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.traffic.fleets.CompanyFleet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DensityBasedTraffic extends Traffic {

  public static class CodeWeight {
    public String code;
    public double weight;

    @Override
    public String toString() {
      return String.format("%s -> %.3f", code, weight);
    }
  }

  public static class CodeWeightList extends ArrayList<CodeWeight>{
    @XmlIgnore
    private double weightSum = -1;

    public CodeWeight getRandomCode() {
      if (weightSum < 0)
        weightSum = CollectionUtils.sum(this, o-> o.weight);
      double rnd = Acc.rnd().nextDouble(0, weightSum);
      int index = 0;
      CodeWeight ret = null;
      while (rnd > 0) {
        CodeWeight cw = this.get(index);
        if (rnd < cw.weight){
          ret = cw;
          break;
        }else {
          rnd -= cw.weight;
          index++;
        }
      }
      assert ret != null;

      return ret;
    }
  }

  public static class DirectionWeight{
    public int heading;
    public double weight;

    @Override
    public String toString() {
      return String.format("%03d -> %.3f", heading, weight);
    }
  }

  public static class HourBlockMovements implements Comparable<HourBlockMovements> {
    public int hour;
    public int arrivals;
    public int departures;

    @Override
    public int compareTo(HourBlockMovements o) {
      return Integer.compare(hour, o.hour);
    }

    @Override
    public String toString() {
      return String.format("%s:00 -> %d / %d", hour, departures, arrivals);
    }
  }

  private CodeWeightList companies = null; // XML
  private CodeWeightList countries = null; // XML
  private List<HourBlockMovements> density = null; //XMl
  private List<DirectionWeight> directions = new ArrayList<>(); // XML
  private double nonCommercialFlightProbability = 0; // XML

  @XmlIgnore
  private Integer lastGeneratedHour = null;

  @Override
  public void generateNewMovementsIfRequired() {
    if (lastGeneratedHour == null || lastGeneratedHour == Acc.now().getHours()) {
      generateNewMovements();
    }
  }

  private void generateNewMovements() {
    if (lastGeneratedHour == null) {
      if (density.size() == 0)
        throw new EApplicationException("Unable to use generic traffic without density specified.");
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
      index++;
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
      prefix = this.countries.getRandomCode().code;
    else
      prefix = this.companies.getRandomCode().code;

    Callsign cls = super.generateUnusedCallsign(prefix, isNonCommercial);
    ETime initTime = new ETime(hour, Acc.rnd().nextInt(0, 59), Acc.rnd().nextInt(0, 59));
    int delay = super.generateDelayMinutes();

    AirplaneType type;
    if (isNonCommercial)
      //TODO here should be some like category probability
      type = Acc.types().getRandom();
    else{
      CompanyFleet cf = Acc.fleets().tryGetByIcao(prefix);
      if (cf == null) cf = Acc.fleets().getDefaultCompanyFleet();
      type = cf.getRandom().getAirplaneType();
    }


    Movement m = new Movement(cls, type, initTime, delay, isDeparture);
    super.addScheduledMovement(m);
  }


}
