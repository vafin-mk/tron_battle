package model;

import java.util.*;

public class Grid {

  private List<Cell> cells = new ArrayList<>();
  private Map<Cycle, Set<Cell>> occupiedCells = new HashMap<>();
  public Map<Cell, Set<Cell>> neighbours = new HashMap<>();
  private Map<Cycle, Integer> distancesToEnemies;

  private List<Cycle> cycles;
  private int myIndex;
  private int playersCount;

  private List<Integer> checkOrder = new ArrayList<>();

  public Grid(int playersCount, int myIndex) {
    this.playersCount = playersCount;
    this.myIndex = myIndex;

    for (int x = 0; x < Constants.GRID_WIDTH; x++) {
      for (int y = 0; y < Constants.GRID_HEIGHT; y++) {
        Cell cell = new Cell(x, y);
        cells.add(cell);
        neighbours.put(cell, new HashSet<>());
      }
    }

    for (Cell cell : cells) {
      if (cell.x > 0) neighbours.get(cell).add(new Cell(cell.x - 1, cell.y));
      if (cell.x < Constants.GRID_WIDTH - 1) neighbours.get(cell).add(new Cell(cell.x + 1, cell.y));
      if (cell.y > 0) neighbours.get(cell).add(new Cell(cell.x, cell.y - 1));
      if (cell.y < Constants.GRID_HEIGHT - 1) neighbours.get(cell).add(new Cell(cell.x, cell.y + 1));
    }

    int index = myIndex;
    while (index < playersCount) {
      checkOrder.add(index);
      index++;
    }
    index = 0;
    while (index < myIndex) {
      checkOrder.add(index);
      index++;
    }
  }

  public void update(List<Cycle> cycles) {
    this.cycles = cycles;

    if (distancesToEnemies == null) {
      distancesToEnemies = new HashMap<>();
      for (Cycle cycle : cycles) {
        if (cycle.index == myIndex) {
          continue;
        }
        distancesToEnemies.put(cycle, -1);
      }
    }

    for (Cycle cycle : cycles) {
      if (cycle.dead()) {
        occupiedCells.remove(cycle);
        distancesToEnemies.remove(cycle);
        continue;
      }
      if (!occupiedCells.containsKey(cycle)) {
        occupiedCells.put(cycle, new HashSet<>());
        occupiedCells.get(cycle).add(cycle.start);
      }
      occupiedCells.get(cycle).add(cycle.head);
    }
    if (myIndex < 10) {//local runner
      updateDistancesToEnemies();
    }
  }

  private void updateDistancesToEnemies() {
    if (distancesToEnemies.isEmpty()) return;
    for (Cycle cycle : distancesToEnemies.keySet()) {
      if (cycle.index == myIndex || cycle.dead()) {
        continue;
      }
      distancesToEnemies.put(cycle, -1);
    }
    Set<Cell> checkedCells = new HashSet<>(allOccupiedCells());
    int dist = 0;
    int updatedEnemies = 0;
    boolean checkedEveryCell;

    List<Cell> starters = new ArrayList<>();
    starters.add(cycles.get(myIndex).head);

    while (true) {
      checkedEveryCell = true;

      Set newCells = new HashSet();
      for (Cell cell : starters) {
        for (Cycle cycle : distancesToEnemies.keySet()) {
          if (cell.equals(cycle.head)) {
            int currDist = distancesToEnemies.get(cycle);
            if (currDist == -1) {
              distancesToEnemies.put(cycle, dist);
              updatedEnemies++;
            }
          }
        }
        if (checkedCells.contains(cell) && dist > 0) continue;
        checkedEveryCell = false;
        checkedCells.add(cell);
        newCells.addAll(neighbours.get(cell));
      }
      starters.clear();
      starters.addAll(newCells);
      newCells.clear();

      if (checkedEveryCell || updatedEnemies == distancesToEnemies.size()) break;
      dist++;
    }
  }

  public boolean blocked(Cell cell) {
    for (Set<Cell> occupied : occupiedCells.values()) {
      if (occupied.contains(cell)) return true;
    }
    return false;
  }

  private List<Cell> allOccupiedCells() {
    List<Cell> result = new ArrayList<>(300);
    occupiedCells.values().forEach(result::addAll);
    return result;
  }

  public void applyMove(Cycle cycle, Cell newHead) {
    occupiedCells.get(cycle).add(newHead);
    cycle.head = newHead;
  }

  public void redoMove(Cycle cycle, Cell oldHead) {
    occupiedCells.get(cycle).remove(cycle.head);
    cycle.head = oldHead;
  }

  public int evaluateCell(Cell neighbour, EvaluationStrategy evaluationStrategy) {
    if (blocked(neighbour)) return Integer.MIN_VALUE;

    switch (evaluationStrategy) {
      case NORMAL: return calculateBelongCellsPriority(neighbour);
      case MINIMAX: return calculateMinimaxPriority(neighbour);
      case SURVIVAL: return calculateSurvivalPriority(neighbour);
    }

    System.err.println("BRAND NEW STRATEGY?: " + evaluationStrategy);
    return 0;//ERROR!
  }

  private int calculateBelongCellsPriority(Cell cell) {
    int[] belongCells = calculateBelongCells(cell);

    if (Constants.DEBUG) {
      System.err.println("--------------");
      System.err.println("Evaluate for:" + cell);
      System.err.println("My cells:" + belongCells[0]);
      System.err.println("Enemy cells:" + belongCells[1]);
      System.err.println("Enemy dists:" + belongCells[2]);
    }
    return belongCells[0] * Constants.MY_BELONG_CELLS_COEFFICIENT
        + belongCells[1] * Constants.ENEMY_BELONG_CELLS_COEFFICIENT
        + belongCells[2] * Constants.ENEMY_BELONG_CELLS_DISTS_COEFFICIENT;
  }

  //belonged cells(which you can access faster than enemy)
  private int[] calculateBelongCells(Cell from) {
    int myCells = 0;
    int enemyCells = 0;
    int enemyDists = 0;

    Set<Cell> checkedCells = new HashSet<>(allOccupiedCells());
    int dist = 1;
    boolean checkedEveryCell;

    Map<Cycle, List<Cell>> starters = new HashMap<>();
    for (Cycle cycle : cycles) {
      if (cycle.dead()) continue;
      starters.put(cycle, new ArrayList<>());
      if (cycle.index == myIndex) {
        starters.get(cycle).addAll(neighbours.get(from));
      } else {
        starters.get(cycle).addAll(neighbours.get(cycle.head));
      }
    }

    while (true) {
      checkedEveryCell = true;

      for (int index = 0; index < checkOrder.size(); index++) {
        Cycle cycle = cycles.get(index);
        if (cycle.dead()) continue;
        Set newCells = new HashSet();
        for (Cell cell : starters.get(cycle)) {
          if (checkedCells.contains(cell)) continue;
          checkedEveryCell = false;
          if (cycle.index == myIndex) {
            myCells++;
          } else {
            enemyCells++;
            enemyDists += dist;
          }
          checkedCells.add(cell);
          newCells.addAll(neighbours.get(cell));
        }
        starters.get(cycle).clear();
        starters.get(cycle).addAll(newCells);
      }

      if (checkedEveryCell) break;
      dist++;
    }

    return new int[] {myCells, enemyCells, enemyDists};
  }

  public int closestDistanceToEnemy() {
    if (Constants.DEBUG) {
      distancesToEnemies.forEach((cycle, dist) -> System.err.println(cycle + " --> " + dist));
    }
    int minDist = Integer.MAX_VALUE;
    for (Integer dist : distancesToEnemies.values()) {
      if (dist > 0 && dist < minDist) {
        minDist = dist;
      }
    }
    if (minDist == Integer.MAX_VALUE) {
      return -1;
    }
    return minDist;
  }

  private int calculateMinimaxPriority(Cell cell) {
    int minimaxValue =
        minimax(cell, Constants.MINIMAX_DEPTH, new int[]{Integer.MIN_VALUE}, new int[]{Integer.MAX_VALUE}, cycles.get(myIndex))
            * Constants.MINIMAX_COEFFICIENT;
    return minimaxValue;
  }

  //minimax https://en.wikipedia.org/wiki/Minimax
  //with alpha-beta pruning https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
  private int minimax(Cell cell, int depth, int[] alpha, int[] beta, Cycle cycle) {
    if (depth == 0 || blocked(cell)) {
      return calculateBelongCellsPriority(cell); //todo blocked ?
    }
    Cell oldHead = cycle.head;
    applyMove(cycle, cell);
    if (cycle.index == myIndex) { //maximize
      int bestValue = Integer.MIN_VALUE;
      for (Cell child : neighbours.get(cell)) {
        int value = minimax(child, depth - 1, alpha, beta, closestEnemyCycle());
        alpha[0] = StrictMath.max(value, alpha[0]);
        bestValue = StrictMath.max(value, bestValue);
        if (alpha[0] >= beta[0]) break;
      }
      redoMove(cycle, oldHead);
      return bestValue;
    } else { //enemy - minimize
      int bestValue = Integer.MAX_VALUE;
      for (Cell child : neighbours.get(cell)) {
        int value = minimax(child, depth - 1, alpha, beta, cycles.get(myIndex));
        beta[0] = StrictMath.min(value, beta[0]);
        bestValue = StrictMath.min(value, bestValue);
        if (alpha[0] >= beta[0]) break;
      }
      redoMove(cycle, oldHead);
      return bestValue;
    }
  }

  private Cycle closestEnemyCycle() {
    Cycle closest = null;
    int closestDist = Constants.MINIMAX_DEPTH;
    for (Map.Entry<Cycle, Integer> enemyEntry : distancesToEnemies.entrySet()) {
      if (enemyEntry.getValue() == -1) {
        continue;
      }
      if (enemyEntry.getValue() <= closestDist) {
        closestDist = enemyEntry.getValue();
        closest = enemyEntry.getKey();
      }
    }
    return closest;
  }

  private int calculateSurvivalPriority(Cell cell) {
    return wallHuggingPriority(cell) * Constants.WALL_HUGG_COEFFICIENT
        + calculateBelongCells(cell)[0];//my cells
  }

  private int wallHuggingPriority(Cell cell) {
    int value = 0;
    for (Cell neigh : neighbours.get(cell)) {
      if (blocked(neigh)) {
        value++;
      }
    }
    if (value > 3) {//dead end!
      return -1;
    }
    return value;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int y = 0; y < Constants.GRID_HEIGHT; y++) {
      row: for (int x = 0; x < Constants.GRID_WIDTH; x++) {
        for (Map.Entry<Cycle, Set<Cell>> entry : occupiedCells.entrySet()) {
          if (entry.getValue().contains(new Cell(x, y))) {
            builder.append(entry.getKey().index);
            continue row;
          }
        }
        builder.append(".");
      }
      builder.append("\n");
    }
    return builder.toString();
  }

}
