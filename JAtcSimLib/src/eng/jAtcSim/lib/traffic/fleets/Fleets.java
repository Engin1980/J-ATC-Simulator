package eng.jAtcSim.lib.traffic.fleets;

import java.util.ArrayList;

public class Fleets extends ArrayList<CompanyFleet> {


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
