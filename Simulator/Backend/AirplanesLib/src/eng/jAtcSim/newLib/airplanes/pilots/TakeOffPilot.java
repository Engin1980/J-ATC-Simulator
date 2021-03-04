package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import exml.loading.XLoadContext; import exml.saving.XSaveContext;
import exml.annotations.XConstructor;

public class TakeOffPilot extends Pilot {
  //TODO add to airport config the acceleration altitude and use it here
  private final ActiveRunwayThreshold takeOffThreshold;

  @XConstructor
  public TakeOffPilot(XLoadContext ctx) {
    super(ctx);
    this.takeOffThreshold = null;
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
          wrt.setState(AirplaneState.takeOff);
        }
        break;
      case takeOff:
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
  public void load(XElement elm, XLoadContext ctx) {
    super.load(elm, ctx);
  }

  @Override
  public void save(XElement elm, XSaveContext ctx) {
    super.save(elm, ctx);
    ctx.fields.saveRemainingFields(this, elm);
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
            AirplaneState.takeOff
    };
  }
}
