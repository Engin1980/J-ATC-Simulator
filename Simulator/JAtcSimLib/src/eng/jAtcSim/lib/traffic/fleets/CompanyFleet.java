package eng.jAtcSim.lib.traffic.fleets;

import eng.eSystem.collections.*;
import eng.eSystem.utilites.CollectionUtils;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;

public class CompanyFleet {
  private static final String DEFAULT_AIRPLANE_TYPE_NAME = "A319";
  public String icao;
  @XmlItemElement(elementName = "type", type = FleetType.class)
  private IList<FleetType> types = new EList<>();
  @XmlIgnore
  private double fleetWeightSum = -1;
  @XmlIgnore
  private IMap<Character, Double> categoryFleetWeightSum = null;

  public IReadOnlyList<FleetType> getTypes() {
    return types;
  }



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
    this.fleetWeightSum = CollectionUtils.sum(this.types, o -> (double) o.weight);
  }

  private void updateCategoryWeightSum() {
    this.categoryFleetWeightSum = new EMap<>();
    this.categoryFleetWeightSum.set('A', 0d);
    this.categoryFleetWeightSum.set('B', 0d);
    this.categoryFleetWeightSum.set('C', 0d);
    this.categoryFleetWeightSum.set('D', 0d);

    for (FleetType fleetType : this.types) {
      double tmp =
          this.categoryFleetWeightSum.get(fleetType.getAirplaneType().category) +
              fleetType.weight;
      this.categoryFleetWeightSum.set(fleetType.getAirplaneType().category, tmp);
    }
  }

  @Override
  public String toString() {
    return String.format("%s (%d types)", this.icao, this.types.size());
  }
}
