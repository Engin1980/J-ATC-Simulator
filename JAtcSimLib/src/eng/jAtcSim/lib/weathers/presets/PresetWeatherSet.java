package eng.jAtcSim.lib.weathers.presets;

import eng.eSystem.collections.EList;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;

@XmlItemElement(elementName = "weather", type = PresetWeather.class)
@XmlItemElement(elementName = "metar", type=PresetWeather.class, parser = MetarParser.class)
public class PresetWeatherSet extends EList<PresetWeather> {
  public void sort(){
    this.sort(q->q.getTime());
  }
}

