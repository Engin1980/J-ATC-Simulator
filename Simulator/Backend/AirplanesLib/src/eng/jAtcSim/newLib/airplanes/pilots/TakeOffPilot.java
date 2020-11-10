package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

public class TakeOffPilot extends Pilot {
  //TODO add to airport config the acceleration altitude and use it here
  private final ActiveRunwayThreshold takeOffThreshold;

  public static TakeOffPilot load(XElement element, IMap<String, Object> context) {
    Airplane airplane = (Airplane) context.get("airplane");
    Airport airport = (Airport) context.get("airport");

    TakeOffPilot ret = new TakeOffPilot(airplane);
    XmlLoadUtils.Field.restoreField(element, ret, "takeOffThreshold",
            (String e) -> airport.getRunwayThreshold(e));

    return ret;
  }

  public TakeOffPilot(Airplane plane) {
    super(plane);
    this.takeOffThreshold = plane.getReader().getRouting().getAssignedRunwayThreshold();
    EAssert.isNotNull(this.takeOffThreshold);
  }

  @Override
  public void elapseSecondInternal() {
    switch (rdr.getState()) {
      case holdingPoint:
        wrt.resetHeading(takeOffThreshold.getCourse());
        wrt.setTargetSpeed(rdr.getType().v2);
        wrt.setState(AirplaneState.takeOffRoll);
        break;
      case takeOffRoll:
        double targetHeading = Coordinates.getBearing(
                rdr.getCoordinate(), takeOffThreshold.getOtherThreshold().getCoordinate());
        wrt.setTargetHeading(new HeadingNavigator(targetHeading, LeftRightAny.any));

        if (rdr.getSha().getSpeed() > rdr.getType().vR) {
          wrt.setState(AirplaneState.takeOffGoAround);
        }
        break;
      case takeOffGoAround:
        // keeps last heading
        // altitude already set
        // speed set
        if (rdr.getSha().getAltitude() > this.takeOffThreshold.getAccelerationAltitude())
          if (rdr.isArrival()) {
            // antecedent G/A
            wrt.startArriving();
          } else {
            wrt.startDeparting();
          }
        break;
      default:
        super.throwIllegalStateException();
    }
  }

  @Override
  public boolean isDivertable() {
    return false;
  }

  @Override
  protected void _save(XElement target) {
    XmlSaveUtils.Field.storeField(target, this, "takeOffThreshold",
            (ActiveRunwayThreshold q) -> q.getFullName());
  }

  @Override
  protected AirplaneState[] getInitialStates() {
    return new AirplaneState[]{
            AirplaneState.holdingPoint
    };
  }

  @Override
  protected AirplaneState[] getValidStates() {
    return new AirplaneState[]{
            AirplaneState.takeOffRoll,
            AirplaneState.takeOffGoAround
    };
  }
}
