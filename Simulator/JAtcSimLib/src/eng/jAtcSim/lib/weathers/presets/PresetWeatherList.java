package eng.jAtcSim.lib.weathers.presets;

import eng.eSystem.collections.EList;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
import eng.eSystem.xmlSerialization.annotations.XmlItemIgnoreElement;

@XmlItemElement(elementName = "weather", type = PresetWeather.class)
@XmlItemElement(elementName = "metar", type=PresetWeather.class, parser = MetarParser.class)
@XmlItemIgnoreElement(elementName = "meta")
public class PresetWeatherList extends EList<PresetWeather> {
  public void sort(){
    this.sort(q->q.getTime());
  }
}

