package eng.jAtcSim.newLib.xml.traffic.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.traffic.models.DensityBasedTrafficModel;

public class DensityBasedTrafficModelXmlLoader {
  public DensityBasedTrafficModel load(XElement elm) {

    double defaultGeneralAviationProbability = Double.parseDouble(elm.getAttribute("defaultGeneralAviationProbability"));
    //TODO isFullDayTraffic not implemented
    boolean isCompaniesFullDayTraffic = elm.getChild("companies").tryGetAttribute("issFullDayTraffic").orElse("false").equals("true");

    IList<DensityBasedTrafficModel.HourBlockMovements> densityLst = new EList<>();
    elm.getChild("density").getChildren("item").forEach(q -> {
      DensityBasedTrafficModel.HourBlockMovements m = new DensityBasedTrafficModel.HourBlockMovements(
              SmartXmlLoaderUtils.loadInteger(q, "arrivals"),
              SmartXmlLoaderUtils.loadInteger(q, "departures"),
              SmartXmlLoaderUtils.loadDouble(q, "generalAviationProbability", defaultGeneralAviationProbability));
      densityLst.add(m);
    });
    DensityBasedTrafficModel.HourBlockMovements[] density = densityLst.toArray(DensityBasedTrafficModel.HourBlockMovements.class);
    EAssert.isTrue(density.length == 24);

    IList<DensityBasedTrafficModel.Company> companies = new EList<>();
    elm.getChild("companies").getChildren("company").forEach(q -> {
      DensityBasedTrafficModel.Company c = new DensityBasedTrafficModel.Company(
              q.getAttribute("name"),
              SmartXmlLoaderUtils.loadInteger(q, "weight"));
      companies.add(c);
    });

    IList<DensityBasedTrafficModel.Company> countries = new EList<>();
    elm.getChild("countries").getChildren("country").forEach(q -> {
      DensityBasedTrafficModel.Company c = new DensityBasedTrafficModel.Company(
              q.getAttribute("name"),
              SmartXmlLoaderUtils.loadInteger(q, "weight"));
      companies.add(c);
    });

    IList<DensityBasedTrafficModel.DirectionWeight> directions = new EList<>();
    elm.getChild("directions").getChildren("direction").forEach(q -> {
      DensityBasedTrafficModel.DirectionWeight d = new DensityBasedTrafficModel.DirectionWeight(
              SmartXmlLoaderUtils.loadInteger(q, "heading"),
              SmartXmlLoaderUtils.loadDouble(q, "weight"));
      directions.add(d);
    });

    DensityBasedTrafficModel ret = DensityBasedTrafficModel.create(
            density, companies, countries, directions);
    return ret;
  }
}
