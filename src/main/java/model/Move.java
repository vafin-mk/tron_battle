package model;

public class Move implements Comparable<Move>{

  public final String command;
  int priority = -1;
  private Move(String command) {
    this.command = command;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public void execute() {
    System.out.println(command);
  }

  public static Move byCells(Cell current, Cell target) {
    int dx = current.x - target.x;
    if (dx > 0) return new Move("LEFT");
    if (dx < 0) return new Move("RIGHT");
    int dy = current.y - target.y;
    if (dy > 0) return new Move("UP");
    return new Move("DOWN");
  }

  @Override
  public int compareTo(Move o) {
    return Integer.compare(priority, o.priority);
  }

  @Override
  public String toString() {
    return command + " - " + priority;
  }
}
