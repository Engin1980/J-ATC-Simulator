package eng.jAtcSim.lib.weathers.presets;

import eng.eSystem.collections.EList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.lib.weathers.decoders.MetarDecoder;

public class PresetWeatherList extends EList<PresetWeather> {
  public static PresetWeatherList load(XElement root) {

    PresetWeatherList ret = new PresetWeatherList();

    for (XElement child : root.getChildren()) {
      if (child.getName().equals("metar")) {
        String metar = child.getContent();
        PresetWeather pw = MetarDecoder.decode(metar);
        ret.add(pw);
      } else if (child.getName().equals("weather")){
        PresetWeather pw = PresetWeather.load(child);
        ret.add(pw);
      }
    }

    ret.sort();

    return ret;
  }

  private PresetWeatherList() {
  }

  private void sort(){
    this.sort(q->q.getTime());
  }
}

