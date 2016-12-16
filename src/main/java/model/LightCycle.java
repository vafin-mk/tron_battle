package model;

import java.util.ArrayList;
import java.util.List;

public class LightCycle {

  public Point startPoint;
  public List<Point> holdedPoints = new ArrayList<>();
  public boolean me;

  public LightCycle(Point startPoint) {
    this.startPoint = startPoint;
  }

  public void addHoldedPosition(Point point) {
    this.holdedPoints.add(point);
  }

  public Point currentPosition() {
    return holdedPoints.get(holdedPoints.size() - 1);
  }
}
