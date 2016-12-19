package model;

public class Point {

  public int x;
  public int y;
  public int holder = -1;
  public Pair pair;
  public Point(int x, int y) {
    this.x = x;
    this.y = y;
    pair = new Pair(x, y);
  }

  public void update(int x, int y) {
    this.x = x;
    this.y = y;
    pair = new Pair(x, y);
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
}
