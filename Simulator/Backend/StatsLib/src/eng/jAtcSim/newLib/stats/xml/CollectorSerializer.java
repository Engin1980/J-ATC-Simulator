package eng.jAtcSim.newLib.stats.xml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.stats.Collector;
import eng.jAtcSim.newLib.stats.properties.StatisticProperty;
import eng.jAtcSimLib.xmlUtils.Serializer;
import eng.jAtcSimLib.xmlUtils.serializers.ObjectSerializer;

public class CollectorSerializer implements Serializer<Collector> {
  private static IMap<Class<?>, Serializer<?>> serializers;

  static {
    serializers = new EMap<>();
    serializers.set(StatisticProperty.class, ObjectSerializer.createFor(StatisticProperty.class));
  }

  private final ObjectSerializer<?> sos;

  public CollectorSerializer() {
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
}
