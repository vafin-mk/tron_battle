package model;

public class Pair {

  public final int x;
  public final int y;

  public Pair(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Pair neighbour(Move move) {
    switch (move) {
      case LEFT:
        return new Pair(x - 1, y);
      case DOWN:
        return new Pair(x, y + 1);
      case UP:
        return new Pair(x, y - 1);
      case RIGHT:
        return new Pair(x + 1, y);
    }
    throw new IllegalArgumentException("Fuckup");
  }

  @Override
  public int hashCode() {
    return x * 31 + y;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Pair)) return false;
    Pair other = (Pair) obj;
    return x == other.x && y == other.y;
  }
}
