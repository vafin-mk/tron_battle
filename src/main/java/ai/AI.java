package ai;

import model.Board;
import model.LightCycle;
import model.Move;
import model.Point;

import java.util.*;

public class AI {

  public static final int WIDTH = 30;
  public static final int HEIGHT = 20;
  public static final int PREDICTION_DEPTH = 10;

  List<LightCycle> cycles = new ArrayList<>();
  LightCycle myCycle;

  private final Board board;
  private boolean firstIteration = true;

  final Scanner scanner;
  final Random rnd;
  public AI(Scanner scanner) {
    this.scanner = scanner;
    this.rnd = new Random();
    this.board = new Board(WIDTH, HEIGHT);
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
    for (int index = 0; index < playersCount; index++) {
      int X0 = scanner.nextInt();
      int Y0 = scanner.nextInt();
      int X1 = scanner.nextInt();
      int Y1 = scanner.nextInt();
      if (firstIteration) {
        LightCycle cycle = new LightCycle(index, myIndex);
        cycle.startPoint = new Point(X0, Y0);
        cycles.add(cycle);
      }
      LightCycle cycle = cycles.get(index);
      cycle.startPoint.update(X0, Y0);
      cycle.startPoint.holder = index;
      cycle.addHoldedPosition(new Point(X1, Y1));
    }
    if (firstIteration) {
      myCycle = cycles.get(myIndex);
      firstIteration = false;
    }
    board.update(cycles, myCycle);
  }

  private void makeDecision() {
    Move best = board.bestMove();
    System.err.println("BEST MOVE:" + best.name());
    best.execute();
  }
}
