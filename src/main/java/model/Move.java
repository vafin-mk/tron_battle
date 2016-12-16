package model;

import java.util.Random;

public enum Move {
  LEFT, UP, RIGHT, DOWN;

  public void execute() {
    System.out.println(name());
  }

  public static Move random(Random rnd) {
    return values()[rnd.nextInt(values().length)];
  }

  public static Move byPoints(Point current, Point target) {
    if (target.x > current.x) return RIGHT;
    if (target.x < current.x) return LEFT;
    if (target.y < current.y) return UP;
    return DOWN;
  }
}
