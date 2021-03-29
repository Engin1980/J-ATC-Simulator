package eng.jAtcSim.newLib.xml.traffic.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.traffic.models.SimpleGenericTrafficModel;

public class SimpleGenericTrafficModelXmlLoader {
  public SimpleGenericTrafficModel load(XElement elm) {

    double defaultProbabilityOfDeparture = Double.parseDouble(elm.getAttribute("double defaultProbabilityOfDeparture"));
    double defaultProbabilityOfGeneralAviation = Double.parseDouble(elm.getAttribute("defaultProbabilityOfGeneralAviation"));

    IList<SimpleGenericTrafficModel.MovementsForHour> movementsForHours = new EList<>();
    elm.getChild("movements").getChildren("movementsForHour").forEach(q -> {
      SimpleGenericTrafficModel.MovementsForHour m = new SimpleGenericTrafficModel.MovementsForHour(
              SmartXmlLoaderUtils.loadInteger(q, "count"),
              SmartXmlLoaderUtils.loadDouble(q, "probabilityOfGeneralAviation", defaultProbabilityOfGeneralAviation),
              SmartXmlLoaderUtils.loadDouble(q, "departureProbability", defaultProbabilityOfDeparture));
      movementsForHours.add(m);
    });

    IList<SimpleGenericTrafficModel.ValueAndWeight> companies = loadItems(elm.getChild("companies"));
    IList<SimpleGenericTrafficModel.ValueAndWeight> countries = loadItems(elm.getChild("countryCodes"));

    SimpleGenericTrafficModel ret = SimpleGenericTrafficModel.create(
            movementsForHours.toArray(SimpleGenericTrafficModel.MovementsForHour.class),
            companies, countries);
    return ret;
  }

  private IList<SimpleGenericTrafficModel.ValueAndWeight> loadItems(XElement source) {
    IList<SimpleGenericTrafficModel.ValueAndWeight> ret = new EList<>();
    source.getChildren("item").forEach(q -> {
      SimpleGenericTrafficModel.ValueAndWeight vw = SimpleGenericTrafficModel.ValueAndWeight.create(
              q.getContent(),
              q.hasAttribute("weight") ? Integer.parseInt(q.getAttribute("weight")) : 1
      );
    });
    return ret;
  }
}
