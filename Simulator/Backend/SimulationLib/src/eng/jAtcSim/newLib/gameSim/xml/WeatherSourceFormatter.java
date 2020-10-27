package eng.jAtcSim.newLib.gameSim.xml;

import eng.eSystem.exceptions.ToDoException;
import eng.jAtcSim.newLib.gameSim.game.sources.WeatherOnlineSource;
import eng.jAtcSim.newLib.gameSim.game.sources.WeatherSource;
import eng.jAtcSim.newLib.gameSim.game.sources.WeatherUserSource;
import eng.jAtcSim.newLib.gameSim.game.sources.WeatherXmlSource;
import eng.jAtcSimLib.xmlUtils.Formatter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class WeatherSourceFormatter implements Formatter<WeatherSource> {
  @Override
  public String invoke(WeatherSource weatherSource) {
    if (weatherSource instanceof WeatherXmlSource)
      return "File;" + ((WeatherXmlSource) weatherSource).getFileName();
    else if (weatherSource instanceof WeatherUserSource)
      throw new ToDoException("Specify serializer for weather and use it here.");
    else if (weatherSource instanceof WeatherOnlineSource) {
      WeatherOnlineSource wos = (WeatherOnlineSource) weatherSource;
      throw new ToDoException("Specify serializer for weather and use it here.");
//        return sf("Online;%s;%s",
//                wos.getIcao(),
//                throw new ToDoException(())
//           )
    } else
      throw new UnsupportedOperationException(sf("Unknown weatherSource type %s.", weatherSource.getClass()));
  }
}
