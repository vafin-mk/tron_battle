package localrunner;

import ai.AI;
import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalRunner {

  private List<AI> ais;
  private List<Cycle> cycles;
  private Grid gameBoard;

  LocalRunner(List<AI> ais) {
    this.ais = ais;
    cycles = new ArrayList<>(ais.size());
    for (int i = 0; i < ais.size(); i++) {
      cycles.add(new Cycle(i));
    }
    gameBoard = new Grid(ais.size(), 10000);
  }

  public void setStartPositions(Cell... cells) {
    for (int i = 0; i < cells.length; i++) {
      cycles.get(i).start = new Cell(cells[i].x, cells[i].y);
      cycles.get(i).head = new Cell(cells[i].x, cells[i].y);
    }
  }

  public void start() {
    int round = 1;
    while (!gameFinished()) {
      if (round % 20 == 0) {
        System.err.println(gameBoard);
      }

      System.err.println(cycles);
      for (int i = 0; i < ais.size(); i++) {
        System.err.println("   ROUND " + round++);
        Cycle cycle = cycles.get(i);
        if (cycle.dead()) {
          continue;
        }
        AI ai = ais.get(i);
        ai.updateDataLocalRunner(ais.size(), i, cyclesToData());
        Move move = ai.makeDecision();
        if (!applyMove(cycle, move)) {
          System.err.println(gameBoard);
          System.err.println(ai.name + " make invalid move " + move);
          cycle.start = new Cell(-1, -1);
          cycle.head = new Cell(-1, -1);
          if (gameFinished()) {
            break;
          }
        }
        gameBoard.update(cycles);
      }
      if(round > 1500){
        System.err.println("Fuck up");
        break;
      }
    }

    gameBoard.update(cycles);
    System.err.println(gameBoard);
    AI winner = ais.get(cycles.stream().filter(cycle -> !cycle.dead()).findFirst().get().index);
    System.err.println("WINNER IS " + winner.name);
  }

  private boolean gameFinished() {
    int alive = 0;
    for (Cycle cycle : cycles) {
      if (!cycle.dead()){
        alive++;
      }
    }
    return alive <= 1;
  }

  private boolean applyMove(Cycle cycle, Move move) {
    Cell head = cycle.head;
    Cell target = null;
    switch (move.command) {
      case "LEFT":
        target = new Cell(head.x - 1, head.y);
        break;
      case "UP":
        target = new Cell(head.x, head.y - 1);
        break;
      case "RIGHT":
        target = new Cell(head.x + 1, head.y);
        break;
      case "DOWN":
        target = new Cell(head.x, head.y + 1);
        break;
    }
    if (target == null
        || target.x < 0 || target.x >= Constants.GRID_WIDTH
        || target.y < 0 || target.y >= Constants.GRID_HEIGHT
        || gameBoard.blocked(target)) {
      return false;
    }
    cycle.head = target;
    return true;
  }

  private int[][] cyclesToData() {
    int[][] res = new int[cycles.size()][4];
    for (int i = 0; i < cycles.size(); i++) {
      Cycle cycle = cycles.get(i);
      res[i][0] = cycle.start.x;
      res[i][1] = cycle.start.y;
      res[i][2] = cycle.head.x;
      res[i][3] = cycle.head.y;
    }
    return res;
  }

  public static void main(String[] args) {
    List<AI> ais = new ArrayList<>();
    ais.add(new AI("THE FIRST", null));
    ais.add(new AI("THE SECOND", null));
    LocalRunner runner = new LocalRunner(ais);
    runner.setStartPositions(new Cell(2,14), new Cell(28, 14));
    runner.start();
  }
}
