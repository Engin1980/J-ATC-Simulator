package eng.jAtcSim.lib.traffic.fleets;

import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;

import java.util.ArrayList;

public class Fleets extends ArrayList<CompanyFleet> {

  public void initAfterLoad (AirplaneTypes types){
    for (CompanyFleet companyFleet : this) {
      companyFleet.bindFleetTypes(types);
    }
  }

  public CompanyFleet tryGetByIcao(String companyIcao) {
    
    CompanyFleet ret = null;
    for (CompanyFleet cf : this) {
      if (cf.icao.equals(companyIcao)) {
        ret = cf;
        break;
      }
    }
    return ret;
  }

  public static CompanyFleet getDefaultCompanyFleet() {
    CompanyFleet ret = CompanyFleet.getDefault();
    return ret;
  }
}
