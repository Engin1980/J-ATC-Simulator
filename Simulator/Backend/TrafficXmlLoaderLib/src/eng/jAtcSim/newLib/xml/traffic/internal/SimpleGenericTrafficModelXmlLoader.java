package eng.jAtcSim.newLib.xml.traffic.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.traffic.models.SimpleGenericTrafficModel;

public class SimpleGenericTrafficModelXmlLoader {
  public SimpleGenericTrafficModel load(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    double defaultProbabilityOfDeparture = SmartXmlLoaderUtils.loadDouble("defaultProbabilityOfDeparture");
    double defaultProbabilityOfGeneralAviation = SmartXmlLoaderUtils.loadDouble("defaultProbabilityOfGeneralAviation");

    IList<SimpleGenericTrafficModel.MovementsForHour> movementsForHours = SmartXmlLoaderUtils.loadList(
        source.getChild("movements").getChildren("movementsForHour"),
        q -> loadMovementsForHour(q, defaultProbabilityOfDeparture, defaultProbabilityOfGeneralAviation));

    IList<SimpleGenericTrafficModel.ValueAndWeight> companies = SmartXmlLoaderUtils.loadList(
        source.getChild("companies").getChildren("item"),
        q -> loadValueAndWeight(q)
    );

    IList<SimpleGenericTrafficModel.ValueAndWeight> countries = SmartXmlLoaderUtils.loadList(
        source.getChild("countryCodes").getChildren("item"),
        q -> loadValueAndWeight(q)
    );

    SimpleGenericTrafficModel ret = SimpleGenericTrafficModel.create(
        movementsForHours.toArray(SimpleGenericTrafficModel.MovementsForHour.class),
        companies, countries);
    return ret;
  }

  private SimpleGenericTrafficModel.ValueAndWeight loadValueAndWeight(XElement source) {
    String value = source.getContent();
    int weight = SmartXmlLoaderUtils.loadInteger(source, "weight", 1);

    SimpleGenericTrafficModel.ValueAndWeight ret = SimpleGenericTrafficModel.ValueAndWeight.create(value,weight);
    return ret;
  }

  private SimpleGenericTrafficModel.MovementsForHour loadMovementsForHour(XElement source,
                                                                          double defaultProbabilityOfDeparture,
                                                                          double defaultProbabilityOfGeneralAviation) {
    SmartXmlLoaderUtils.setContext(source);

    int count = SmartXmlLoaderUtils.loadInteger("count");
    double departureProbability = SmartXmlLoaderUtils.loadDouble("departureProbability", defaultProbabilityOfDeparture);
    double gaProbability = SmartXmlLoaderUtils.loadDouble("probabilityOfGeneralAviation", defaultProbabilityOfGeneralAviation);

    SimpleGenericTrafficModel.MovementsForHour ret = SimpleGenericTrafficModel.MovementsForHour.create(count, gaProbability, departureProbability);
    return ret;
  }
}
