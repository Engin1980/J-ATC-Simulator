package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.time.EDayTime;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.deserializers.EntriesWithListValuesDeserializer;
import eng.jAtcSimLib.xmlUtils.deserializers.ObjectDeserializer;
import eng.jAtcSimLib.xmlUtils.serializers.EntriesWithListValuesSerializer;
import eng.jAtcSimLib.xmlUtils.serializers.ObjectSerializer;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class TrafficProvider {
  public static TrafficProvider load(XElement element, ITrafficModel trafficModel) {

    System.out.println("Traffic-model not loaded");

    TrafficProvider ret = new TrafficProvider(trafficModel);

    XmlLoadUtils.Field.restoreField(element, ret, "movementsForDay",
            new EntriesWithListValuesDeserializer(
                    e -> Integer.parseInt(e.getContent()),
                    ObjectDeserializer.createEmpty(),
                    () -> new EList<>(),
                    ret.movementsForDay));

    return ret;
  }

  private final ITrafficModel trafficModel;
  private final IMap<Integer, IList<MovementTemplate>> movementsForDay = new EMap<>();


  public TrafficProvider(ITrafficModel trafficModel) {
    EAssert.isNotNull(trafficModel);
    this.trafficModel = trafficModel;
  }

  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay(int dayIndex) {
    throw new ToDoException();
  }

  public IReadOnlyList<MovementTemplate> getMovementsUntilTime(EDayTime untilTime) {
    EAssert.Argument.isNotNull(untilTime, "untilTime");
    EAssert.isTrue(movementsForDay.containsKey(untilTime.getDays()));

    IList<MovementTemplate> ret = new EList<>();
    for (Integer dayKey : movementsForDay.getKeys()) {
      if (dayKey > untilTime.getDays()) continue;
      IReadOnlyList<MovementTemplate> tmp = movementsForDay.get(dayKey).where(q -> q.getAppearanceTime().isBeforeOrEq(untilTime.getTime()));
      ret.addMany(tmp);
      movementsForDay.get(dayKey).removeMany(tmp);
    }

    // delete empty days
    movementsForDay.removeMany(
            movementsForDay.whereValue(q -> q.isEmpty()).getKeys());

    return ret;
  }

  public void prepareTrafficForDay(int dayIndex) {
    EAssert.isTrue(
            movementsForDay.containsKey(dayIndex) == false,
            sf("Cannot generate movements for day %d, as it has been already generated.", dayIndex));
    prepareMovementsForDay(dayIndex);
  }

  public void init() {
    // intentionally blank
  }

  private void prepareMovementsForDay(int dayIndex) {
    IReadOnlyList<MovementTemplate> tmp = trafficModel.generateMovementsForOneDay();
    IList<MovementTemplate> oth = new EList<>(tmp);
    movementsForDay.set(dayIndex, oth);
  }
}
