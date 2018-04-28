package eng.jAtcSim.lib.global.sources;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.weathers.WeatherProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class WeatherSource extends Source<WeatherProvider> {

  private static IMap<String, Class> providers = new EMap<>();
  @XmlIgnore
  private WeatherProvider provider;
  private String weatherProviderClassName;
  private Weather weather;

  static {
    IReadOnlyList<Class> providers;
    try {
      providers = HelpMe.getClasses("eng.jAtcSim.lib.weathers");
    } catch (ClassNotFoundException | IOException e) {
      throw new EApplicationException(e);
    }

    providers = providers.where(q -> WeatherProvider.class.isAssignableFrom(q));

    for (Class provider : providers) {
      WeatherSource.providers.set(provider.getSimpleName(), provider);
    }
  }

  public WeatherSource(String weatherProviderClassName) {
    this.weatherProviderClassName = weatherProviderClassName;
  }

  public WeatherSource(){}

  public void init(){
    super.setInitialized();

    Class c = providers.tryGet(weatherProviderClassName);
    try {
      this.provider = (WeatherProvider) c.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new EApplicationException(sf("Weather provider %s probably does not have public parameter-less constructor.", weatherProviderClassName), e);
    }
    provider.getWeatherUpdatedEvent().add(()-> this.weather = provider.getWeather());
  }

  public void elapseSecond(){
    provider.elapseSecond();
  }

  @Override
  public WeatherProvider _get() {
    return provider;
  }
}


class HelpMe {
  public static IReadOnlyList<Class> getClasses(String packageName)
      throws ClassNotFoundException, IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    assert classLoader != null;
    String path = packageName.replace('.', '/');
    Enumeration<URL> resources = classLoader.getResources(path);
    List<File> dirs = new ArrayList<File>();
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      dirs.add(new File(resource.getFile()));
    }
    ArrayList<Class> classes = new ArrayList<Class>();
    for (File directory : dirs) {
      classes.addAll(findClasses(directory, packageName));
    }

    EList<Class> ret = new EList<>(classes);
    return ret;
  }

  /**
   * Recursive method used to find all classes in a given directory and subdirs.
   *
   * @param directory   The base directory
   * @param packageName The package name for classes found inside the base directory
   * @return The classes
   * @throws ClassNotFoundException
   */
  private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
    List<Class> classes = new ArrayList<Class>();
    if (!directory.exists()) {
      return classes;
    }
    File[] files = directory.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        assert !file.getName().contains(".");
        classes.addAll(findClasses(file, packageName + "." + file.getName()));
      } else if (file.getName().endsWith(".class")) {
        classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
      }
    }
    return classes;
  }
}