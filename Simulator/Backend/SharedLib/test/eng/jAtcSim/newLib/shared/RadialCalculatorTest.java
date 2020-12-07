package eng.jAtcSim.newLib.shared;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.CoordinateValue;
import eng.eSystem.geo.Headings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RadialCalculatorTest {

  @Test
  public void afterAlignedCenter() {
    Coordinate fix = new Coordinate(
            new CoordinateValue(50, 0, 0, false),
            new CoordinateValue(17, 0, 0, false));
    Coordinate plane = new Coordinate(
            new CoordinateValue(50, 0, 0, false),
            new CoordinateValue(16, 50, 0, false));

    int radial = 270;
    int speed = 140;
    int maxHeadingDifference = 30;

    double act = RadialCalculator.getHeadingToFollowRadial(plane, fix, radial, maxHeadingDifference, speed);
    double exp = 270;

    assertEquals(exp, act, 1);
  }

  @Test
  public void beforeAlignedCenter() {
    Coordinate fix = new Coordinate(
            new CoordinateValue(50, 0, 0, false),
            new CoordinateValue(17, 0, 0, false));
    Coordinate plane = new Coordinate(
            new CoordinateValue(50, 0, 0, false),
            new CoordinateValue(17, 10, 0, false));

    int radial = 270;
    int speed = 140;
    int maxHeadingDifference = 30;

    double act = RadialCalculator.getHeadingToFollowRadial(plane, fix, radial, maxHeadingDifference, speed);
    double exp = 270;

    assertEquals(exp, act, 1);
  }

  @Test
  public void beforeAlignedLeft() {
    Coordinate fix = new Coordinate(
            new CoordinateValue(50, 0, 0, false),
            new CoordinateValue(17, 0, 0, false));
    Coordinate plane = new Coordinate(
            new CoordinateValue(50, 1, 0, false),
            new CoordinateValue(17, 10, 0, false));

    int radial = 270;
    int speed = 140;
    int maxHeadingDifference = 30;

    double act = RadialCalculator.getHeadingToFollowRadial(plane, fix, radial, maxHeadingDifference, speed);
    double exp = 267;

    assertEquals(exp, act, 1);
  }

  @Test
  public void beforeAlignedRight() {
    Coordinate fix = new Coordinate(
            new CoordinateValue(50, 0, 0, false),
            new CoordinateValue(17, 0, 0, false));
    Coordinate plane = new Coordinate(
            new CoordinateValue(49, 59, 0, false),
            new CoordinateValue(17, 10, 0, false));

    int radial = 270;
    int speed = 140;
    int maxHeadingDifference = 30;

    double act = RadialCalculator.getHeadingToFollowRadial(plane, fix, radial, maxHeadingDifference, speed);
    double exp = 273;

    assertEquals(exp, act, 1);
  }

  @Test
  public void afterAlignedLeft() {
    Coordinate fix = new Coordinate(
            new CoordinateValue(50, 0, 0, false),
            new CoordinateValue(17, 0, 0, false));
    Coordinate plane = new Coordinate(
            new CoordinateValue(50, 1, 0, false),
            new CoordinateValue(16, 50, 0, false));

    int radial = 270;
    int speed = 140;
    int maxHeadingDifference = 30;

    double act = RadialCalculator.getHeadingToFollowRadial(plane, fix, radial, maxHeadingDifference, speed);
    double exp = 267;

    assertEquals(exp, act, 1);
  }

  @Test
  public void afterAlignedRight() {
    Coordinate fix = new Coordinate(
            new CoordinateValue(50, 0, 0, false),
            new CoordinateValue(17, 0, 0, false));
    Coordinate plane = new Coordinate(
            new CoordinateValue(49, 59, 0, false),
            new CoordinateValue(16, 50, 0, false));

    int radial = 270;
    int speed = 140;
    int maxHeadingDifference = 30;

    double act = RadialCalculator.getHeadingToFollowRadial(plane, fix, radial, maxHeadingDifference, speed);
    double exp = 273;

    assertEquals(exp, act, 1);
  }

  @Test
  public void beforeCloseLeft() {
    Coordinate fix = new Coordinate(
            new CoordinateValue(50, 0, 0, false),
            new CoordinateValue(17, 0, 0, false));
    Coordinate plane = new Coordinate(
            new CoordinateValue(50, 10, 0, false),
            new CoordinateValue(17, 10, 0, false));

    int radial = 270;
    int speed = 140;
    int maxHeadingDifference = 30;

    double act = RadialCalculator.getHeadingToFollowRadial(plane, fix, radial, maxHeadingDifference, speed);
    double exp = 255;

    assertEquals(exp, act, 1);
  }

  @Test
  public void beforeCloseRight() {
    Coordinate fix = new Coordinate(
            new CoordinateValue(50, 0, 0, false),
            new CoordinateValue(17, 0, 0, false));
    Coordinate plane = new Coordinate(
            new CoordinateValue(49, 50, 0, false),
            new CoordinateValue(17, 10, 0, false));

    int radial = 270;
    int speed = 140;
    int maxHeadingDifference = 30;

    double act = RadialCalculator.getHeadingToFollowRadial(plane, fix, radial, maxHeadingDifference, speed);
    double exp = 285;

    assertEquals(exp, act, 1);
  }

  @Test
  public void beforeFarLeft() {
    Coordinate fix = new Coordinate(
            new CoordinateValue(50, 0, 0, false),
            new CoordinateValue(17, 0, 0, false));
    Coordinate plane = new Coordinate(
            new CoordinateValue(51, 0, 0, false),
            new CoordinateValue(17, 10, 0, false));

    int radial = 270;
    int speed = 140;
    int maxHeadingDifference = 90;

    double act = RadialCalculator.getHeadingToFollowRadial(plane, fix, radial, maxHeadingDifference, speed);
    double exp = 180;

    assertEquals(exp, act, 1);
  }

  @Test
  public void beforeFarRight() {
    Coordinate fix = new Coordinate(
            new CoordinateValue(50, 0, 0, false),
            new CoordinateValue(17, 0, 0, false));
    Coordinate plane = new Coordinate(
            new CoordinateValue(49, 0, 0, false),
            new CoordinateValue(17, 10, 0, false));

    int radial = 270;
    int speed = 140;
    int maxHeadingDifference = 90;

    double act = RadialCalculator.getHeadingToFollowRadial(plane, fix, radial, maxHeadingDifference, speed);
    act = Headings.to(act);
    double exp = 0;

    assertEquals(exp, act, 1);
  }
}
