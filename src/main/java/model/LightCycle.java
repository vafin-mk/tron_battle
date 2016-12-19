package model;

import java.util.ArrayList;
import java.util.List;

public class LightCycle {

  public Point startPoint;
  public final boolean me;
  public Point currentPoint;
  public final int index;

  public LightCycle(int index, int myIndex) {
    this.index = index;
    this.me = index == myIndex;
  }

  public void addHoldedPosition(Point point) {
    point.holder = index;
    this.currentPoint = point;
  }

  public boolean isDead() {
    return startPoint.x == -1;
  }

}
