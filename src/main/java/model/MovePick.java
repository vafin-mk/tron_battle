package model;

public class MovePick implements Comparable<MovePick> {

  public final Move move;
  public final int priority;

  public MovePick(Move move, int priority) {
    this.move = move;
    this.priority = priority;
  }

  @Override
  public int compareTo(MovePick o) {
    return Integer.compare(o.priority, priority);
  }
}
