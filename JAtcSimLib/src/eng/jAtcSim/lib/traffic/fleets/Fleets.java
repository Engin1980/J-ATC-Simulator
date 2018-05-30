package eng.jAtcSim.lib.traffic.fleets;

import com.sun.javafx.iio.common.ImageLoaderImpl;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;

import java.util.ArrayList;

public class Fleets extends EList<CompanyFleet> {

  public void init(AirplaneTypes types){
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

  public String[] getIcaos() {
    IList<String> lst = this.select(q->q.icao);
    String[] ret = lst.toArray(String.class);
    return ret;
  }
}
