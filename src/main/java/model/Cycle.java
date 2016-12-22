package model;

public class Cycle {

  public final int index;

  public Cell start;
  public Cell head;

  public Cycle(int index) {
    this.index = index;
  }

  public boolean dead() {
    return start.x == -1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Cycle cycle = (Cycle) o;

    return index == cycle.index;

  }

  @Override
  public int hashCode() {
    return index;
  }
}
