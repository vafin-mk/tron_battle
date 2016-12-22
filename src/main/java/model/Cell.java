package model;

public class Cell {

  public final int x;
  public final int y;

  public Cell(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Cell cell = (Cell) o;

    if (x != cell.x) return false;
    return y == cell.y;

  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }

  @Override
  public String toString() {
    return "Cell("+x+"|"+y+")";
  }
}
