package model;

import java.util.ArrayList;
import java.util.List;

public class LightCycle {

  public Point start;
  public final boolean me;
  public Point head;
  public final int index;

  public LightCycle(int index, int myIndex) {
    this.index = index;
    this.me = index == myIndex;
  }

  public void setHead(Point head) {
    this.head = head;
    if (this.head != null) {
      this.head.holder = index;
    }
  }

  public void setStart(Point start) {
    this.start = start;
    if (this.start != null) {
      this.start.holder = index;
    }
  }

  public void kill() {
    start = new Point(-1, -1);
    head = new Point(-1, -1);
  }

  public boolean isDead() {
    return head.x == -1;
  }

  @Override
  public String toString() {
    return String.format("LightCycle(%s), startPoint(%s), me=%s, dead=%s", index, start, me, isDead());
  }
}
