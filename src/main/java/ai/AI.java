package ai;

import model.LightCycle;
import model.Move;
import model.Point;

import java.util.*;

public class AI {

  public static final int WIDTH = 30;
  public static final int HEIGHT = 20;

  List<LightCycle> cycles;
  LightCycle myCycle;

  List<Point> allHoldedPoints = new ArrayList<>();

  final Scanner scanner;
  final Random rnd;
  public AI(Scanner scanner) {
    this.scanner = scanner;
    this.rnd = new Random();
  }

  public void start() {
    while (true) {
      updateData();
      makeDecision();
    }
  }

  private void updateData() {
    int playersCount = scanner.nextInt();
    int myIndex = scanner.nextInt();
    if (cycles == null) {
      createCycles(playersCount, myIndex);
    }
    for (int index = 0; index < playersCount; index++) {
      LightCycle cycle = cycles.get(index);
      int X0 = scanner.nextInt();
      int Y0 = scanner.nextInt();
      cycle.startPoint.update(X0, Y0);
      int X1 = scanner.nextInt();
      int Y1 = scanner.nextInt();
      cycle.addHoldedPosition(new Point(X1, Y1));
      cycle.me = index == myIndex;
    }
    allHoldedPoints.clear();
    cycles.forEach(cycle -> {
        if (cycle.startPoint.x != 1) allHoldedPoints.addAll(cycle.holdedPoints);
      }
    );
  }

  private void createCycles(int count, int myIndex) {
    cycles = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      cycles.add(new LightCycle(new Point(0, 0)));
    }
    myCycle = cycles.get(myIndex);
  }

  private List<Point> availableMoves(Point from) {
    List<Point> res = new ArrayList<>();
    if (from.x > 0) res.add(new Point(from.x - 1, from.y)); //left
    if (from.y > 0) res.add(new Point(from.x, from.y - 1)); //up
    if (from.x < WIDTH - 1) res.add(new Point(from.x + 1, from.y)); //right
    if (from.y < HEIGHT - 1) res.add(new Point(from.x, from.y + 1)); //down
    res.removeIf(point -> allHoldedPoints.contains(point));
    return res;
  }

  private void makeDecision() {
    Point myPosition = myCycle.currentPosition();
    List<Point> targets = availableMoves(myPosition);
    if (targets.isEmpty()) {
      System.err.println("GG");
      Move.random(rnd).execute();
      return;
    }
    Point bestTarget = targets.get(rnd.nextInt(targets.size()));
    Move.byPoints(myPosition, bestTarget).execute();
  }
}
