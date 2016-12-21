package model;

public class Point {

  public final int x;
  public final int y;
  public int holder = -1;
  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int manhattanDist(Point other) {
    return StrictMath.abs(other.x - x) + StrictMath.abs(other.y - y);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Point point = (Point) o;
    return x == point.x && y == point.y;
  }

  @Override
  public int hashCode() {
    return 31 * x + y;
  }

  @Override
  public String toString() {
    return String.format("Point(%s|%s)", x, y);
  }
}
