package model;

import java.util.*;

public class Grid {

  private List<Cell> cells = new ArrayList<>();
  private Map<Cycle, Set<Cell>> occupiedCells = new HashMap<>();
  public Map<Cell, Set<Cell>> neighbours = new HashMap<>();

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
    for (Cycle cycle : cycles) {
      if (cycle.dead()) {
        occupiedCells.remove(cycle);
      }
      if (!occupiedCells.containsKey(cycle)) {
        occupiedCells.put(cycle, new HashSet<>());
        occupiedCells.get(cycle).add(cycle.start);
      }
      occupiedCells.get(cycle).add(cycle.head);
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

  public int evaluateCell(Cell neighbour) {
    if (blocked(neighbour)) return Integer.MIN_VALUE;
    int[] belongCells = calculateBelongCells(neighbour);

    if (Constants.DEBUG) {
      System.err.println("--------------");
      System.err.println("Evaluate for:" + neighbour);
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
