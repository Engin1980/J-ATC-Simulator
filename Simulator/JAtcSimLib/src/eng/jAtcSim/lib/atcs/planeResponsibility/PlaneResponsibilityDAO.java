package eng.jAtcSim.lib.atcs.planeResponsibility;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneRead;
import eng.jAtcSim.lib.atcs.Atc;

class PlaneResponsibilityDAO {
  private final IList<AirplaneResponsibilityInfo> all = new EList<>();
  @XmlIgnore
  private final IList<Airplane.Airplane4Display> displays = new EList<>();

  public void init() {
    for (AirplaneResponsibilityInfo ai : all) {
      displays.add(ai.getPlane().getPlane4Display());
    }
  }

  public AirplaneResponsibilityInfo get(IAirplaneRead plane) {
    AirplaneResponsibilityInfo ret = all.getFirst(q -> q.getPlane() == plane);
    return ret;
  }

  public IList<AirplaneResponsibilityInfo> getAll() {
    return all;
  }

  public void add(AirplaneResponsibilityInfo ari) {
    this.all.add(ari);
    this.displays.add(ari.getPlane().getPlane4Display());
  }

  public void remove(AirplaneResponsibilityInfo ari) {
    this.all.remove(ari);
    this.displays.remove(ari.getPlane().getPlane4Display());
  }

  public IReadOnlyList<Airplane.Airplane4Display> getDisplays() {
    return this.displays;
  }

  public IReadOnlyList<AirplaneResponsibilityInfo> getByAtc(Atc atc) {
    IReadOnlyList<AirplaneResponsibilityInfo> ret = all.where(q -> q.getAtc() == atc);
    return ret;
  }

  public AirplaneResponsibilityInfo get(Callsign callsign) {
    AirplaneResponsibilityInfo ari = this.all.getFirst(q -> q.getPlane().getFlightModule().getCallsign().equals(callsign));
    return ari;
  }
}
