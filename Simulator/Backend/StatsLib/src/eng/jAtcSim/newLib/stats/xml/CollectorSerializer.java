package eng.jAtcSim.newLib.stats.xml;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.xml.SharedXmlUtils;
import eng.jAtcSim.newLib.stats.Collector;
import eng.jAtcSim.newLib.stats.properties.StatisticProperty;
import eng.jAtcSimLib.xmlUtils.Serializer;
import eng.jAtcSimLib.xmlUtils.serializers.ItemsSerializer;
import eng.jAtcSimLib.xmlUtils.serializers.ObjectSerializer;

public class CollectorSerializer implements Serializer<Collector> {
  private final IMap<Class<?>, Serializer<?>> serializers = new EMap<>();
  private final ObjectSerializer<?> sos;

  public CollectorSerializer() {
    initSerializers();
    this.sos = ObjectSerializer.create()
            .withoutFieldNamesValidation()
            .withStoredType()
            .applyRecursivelyOnObjectClass()
            .useSerializers(serializers);
  }

  @Override
  public void invoke(XElement targetElement, Collector value) {
    sos.invoke(targetElement, value);
  }

  void initSerializers() {
    serializers.set(StatisticProperty.class, ObjectSerializer.createFor(StatisticProperty.class));
    serializers.set(EDayTimeStamp.class, SharedXmlUtils.iTimeSerializer);
    serializers.set(Callsign.class, SharedXmlUtils.callsignSerializer);
    serializers.set(EList.class, new ItemsSerializer<>(sos));
  }
}
