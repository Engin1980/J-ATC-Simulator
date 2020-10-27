package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.utilites.CollectionUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.time.EDayTime;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.serializers.EntriesWithListValuesSerializer;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class TrafficProvider {
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
    movementsForDay.remove(
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

  public void save(XElement target) {
    XmlSaveUtils.Field.storeField(target, this, "movementsForDay",
            new EntriesWithListValuesSerializer<Integer, MovementTemplate>(
                    (e, q) -> e.setContent(Integer.toString(q)),
                    (e, q) -> q.save(e)));

    //Todel
    System.out.println("### trafficModel not saved");
    // traffic model should be loaded from startup info?
  }

  private void prepareMovementsForDay(int dayIndex) {
    IReadOnlyList<MovementTemplate> tmp = trafficModel.generateMovementsForOneDay();
    IList<MovementTemplate> oth = new EList<>(tmp);
    movementsForDay.set(dayIndex, oth);
  }
}
