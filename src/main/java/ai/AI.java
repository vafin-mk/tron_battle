package ai;

import model.*;

import java.util.*;

public class AI {

  final Scanner scanner;
  public final String name;
  private Grid grid;
  private List<Cycle> cycles = new ArrayList<>();
  private int round;

  private int myIndex;

  private Queue<Move> moves = new PriorityQueue<>(Comparator.reverseOrder());

  public AI(String name, Scanner scanner) {
    this.name = name;
    this.scanner = scanner;
  }

  public void start() {
    long start = 0;
    while (true) {
      start = System.nanoTime();
      updateData();
      if (Constants.DEBUG) {
        System.err.println(String.format("UPDATE IN %s ms", (System.nanoTime() - start) / 1000000));
      }
      start = System.nanoTime();
      makeDecision().execute();
      if (Constants.DEBUG) {
        System.err.println(String.format("DECISION IN %s ms", (System.nanoTime() - start) / 1000000));
      }
      round++;
    }
  }

  public void updateData() {
    int playersCount = scanner.nextInt();
    myIndex = scanner.nextInt();
    if (grid == null) {
      grid = new Grid(playersCount, myIndex);
    }
    for (int index = 0; index < playersCount; index++) {
      if (round == 0) {
        cycles.add(new Cycle(index));
      }
      int X0 = scanner.nextInt();
      int Y0 = scanner.nextInt();
      int X1 = scanner.nextInt();
      int Y1 = scanner.nextInt();
      cycles.get(index).start = new Cell(X0, Y0);
      cycles.get(index).head = new Cell(X1, Y1);
    }
    grid.update(cycles);
    round++;
  }

  public void updateDataLocalRunner(int playersCount, int myIndex, int[]...cyclesData) {
    this.myIndex = myIndex;
    if (grid == null) {
      grid = new Grid(playersCount, myIndex);
    }
    for (int i = 0; i < playersCount; i++) {
      if (round == 0) {
        cycles.add(new Cycle(i));
      }
      cycles.get(i).start = new Cell(cyclesData[i][0], cyclesData[i][1]);
      cycles.get(i).head = new Cell(cyclesData[i][2], cyclesData[i][3]);
    }
    grid.update(cycles);
    round++;
  }

  public Move makeDecision() {
    moves.clear();
    Cycle myCycle = cycles.get(myIndex);
    Cell currentHead = myCycle.head;
    for (Cell neighbour : grid.neighbours.get(currentHead)) {
      Move move = Move.byCells(currentHead, neighbour);
//      grid.applyMove(myCycle, neighbour);
      move.setPriority(grid.evaluateCell(neighbour));
//      grid.redoMove(myCycle, currentHead);
      moves.add(move);
    }
    if (Constants.DEBUG) {
      System.err.println(Arrays.toString(moves.toArray()));
    }
    return moves.poll();
  }
}
