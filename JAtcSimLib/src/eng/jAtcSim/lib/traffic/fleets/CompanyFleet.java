package eng.jAtcSim.lib.traffic.fleets;

import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.eSystem.utilites.CollectionUtil;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyFleet {
  private static final String DEFAULT_AIRPLANE_TYPE_NAME = "A319";
  public String icao;
  private List<FleetType> types = new ArrayList<>();

  @XmlIgnore
  private double fleetWeightSum = -1;
  @XmlIgnore
  private Map<Character, Double> categoryFleetWeightSum = null;

  public static CompanyFleet getDefault() {
    CompanyFleet ret = new CompanyFleet();
    ret.icao = "(DEF)";
    FleetType ft = new FleetType();
    ft.name = DEFAULT_AIRPLANE_TYPE_NAME;
    ft.weight = 1;
    ret.types.add(ft);
    return ret;
  }

  public FleetType getRandom() {
    FleetType ret = null;

    if (fleetWeightSum < 0) updateFleetWeightSum();

    double tmp = Acc.rnd().nextDouble(this.fleetWeightSum);
    for (int i = 0; i < this.types.size(); i++) {
      FleetType ft = this.types.get(i);
      if (tmp < ft.weight) {
        ret = ft;
        break;
      } else {
        tmp -= ft.weight;
      }
    }

    assert ret != null;
    return ret;
  }

  public FleetType tryGetRandomByCategory(char category) {
    FleetType ret = null;
    if (categoryFleetWeightSum == null) updateCategoryWeightSum();

    double catWeight = this.categoryFleetWeightSum.get(category);
    if (catWeight == 0)
      return null; // no types for category

    double tmp = Acc.rnd().nextDouble(catWeight);
    for (int i = 0; i < this.types.size(); i++) {
      FleetType ft = this.types.get(i);
      if (ft.getAirplaneType().category != category) continue;
      if (tmp < ft.weight) {
        ret = ft;
        break;
      } else {
        tmp -= ft.weight;
      }
    }

    assert ret != null;
    return ret;
  }

  public void bindFleetTypes(AirplaneTypes types) {
    for (FleetType type : this.types) {
      type.bindFleetType(types);
    }
  }

  private void updateFleetWeightSum() {
    this.fleetWeightSum = CollectionUtil.sum(this.types, o -> o.weight);
  }

  private void updateCategoryWeightSum() {
    this.categoryFleetWeightSum = new HashMap<>();
    this.categoryFleetWeightSum.put('A', 0d);
    this.categoryFleetWeightSum.put('B', 0d);
    this.categoryFleetWeightSum.put('C', 0d);
    this.categoryFleetWeightSum.put('D', 0d);

    for (FleetType fleetType : this.types) {
      double tmp =
          this.categoryFleetWeightSum.get(fleetType.getAirplaneType().category) +
              fleetType.weight;
      this.categoryFleetWeightSum.put(fleetType.getAirplaneType().category, tmp);
    }
  }

  @Override
  public String toString() {
    return String.format("%s (%d types)", this.icao, this.types.size());
  }
}
