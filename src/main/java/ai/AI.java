package ai;

import model.*;

import java.util.*;

public class AI {

  public static final int WIDTH = 30;
  public static final int HEIGHT = 20;
  public static final int PREDICTION_DEPTH = 100;

  List<LightCycle> cycles = new ArrayList<>();
  LightCycle myCycle;

  private final Board board;
  private boolean firstIteration = true;

  final Scanner scanner;
  final Random rnd;
  public final String name;
  public AI(String name, Scanner scanner) {
    this.name = name;
    this.scanner = scanner;
    this.rnd = new Random();
    this.board = new Board(WIDTH, HEIGHT);
  }

  public void start() {
    long start = 0;
    while (true) {
      start = System.nanoTime();
      updateData();
      System.err.println(String.format("UPDATE IN %s ms", (System.nanoTime() - start)/1000000));
      start = System.nanoTime();
      makeDecision().execute();
      System.err.println(String.format("DECISION IN %s ms", (System.nanoTime() - start)/1000000));
    }
  }

  public void updateData() {
    int playersCount = scanner.nextInt();
    int myIndex = scanner.nextInt();
    for (int index = 0; index < playersCount; index++) {
      int X0 = scanner.nextInt();
      int Y0 = scanner.nextInt();
      int X1 = scanner.nextInt();
      int Y1 = scanner.nextInt();
      if (firstIteration) {
        LightCycle cycle = new LightCycle(index, myIndex);
        cycles.add(cycle);
      }
      LightCycle cycle = cycles.get(index);
      cycle.setStart(board.getPoint(X0, Y0));
      cycle.setHead(board.getPoint(X1, Y1));
    }
    if (firstIteration) {
      myCycle = cycles.get(myIndex);
      firstIteration = false;
    }
    board.update(cycles);
  }

  public void updateDataLocalRunner(int playersCount, int myIndex, int[]...cyclesData) {
    for (int i = 0; i < playersCount; i++) {
      System.err.println(Arrays.toString(cyclesData[i]));
      if (firstIteration) {
        LightCycle cycle = new LightCycle(i, myIndex);
        cycles.add(cycle);
      }
      LightCycle cycle = cycles.get(i);
      cycle.setStart(board.getPoint(cyclesData[i][0], cyclesData[i][1]));
      cycle.setHead(board.getPoint(cyclesData[i][2], cyclesData[i][3]));
    }
    if (firstIteration) {
      myCycle = cycles.get(myIndex);
      firstIteration = false;
    }
    board.update(cycles);
  }

  public Move makeDecision() {
    Queue<MovePick> commands = new PriorityQueue<>();
    Point start = myCycle.head;
    for (Point point : board.availableMoves(myCycle)) {
      //apply
      myCycle.setHead(point);

      Move move = Move.byPoints(start, point);
      commands.add(new MovePick(move, board.evaluate(myCycle) + move.ordinal()));

      //undo
      point.holder = -1;
      myCycle.head = start;
    }
    System.err.println("Commands - " + Arrays.toString(commands.toArray()));
    MovePick best = commands.poll();
    System.err.println("BEST MOVE:" + best);
    if (best == null) {
      System.err.println("GG, NO MOVE");
      return Move.UP;
    }
    return best.move;
  }
}
