package localrunner;

import ai.AI;
import model.Board;
import model.LightCycle;
import model.Move;
import model.Point;

import java.util.ArrayList;
import java.util.List;

public class LocalRunner {

  private List<AI> ais;
  private List<LightCycle> cycles;
  private Board gameBoard;

  LocalRunner(List<AI> ais) {
    this.ais = ais;
    cycles = new ArrayList<>(ais.size());
    for (int i = 0; i < ais.size(); i++) {
      cycles.add(new LightCycle(i, 100500));
    }
    gameBoard = new Board(30, 20);
  }

  public void setStartPositions(Point...points) {
    for (int i = 0; i < points.length; i++) {
      cycles.get(i).setStart(gameBoard.getPoint(points[i].x, points[i].y));
      cycles.get(i).setHead(gameBoard.getPoint(points[i].x, points[i].y));
    }
  }

  public void start() {
    int round = 1;
    while (!gameFinished()) {
      if (round % 20 == 0) {
        System.err.println(gameBoard);
      }
      System.err.println("   ROUND " + round++);
      System.err.println(cycles);
      for (int i = 0; i < ais.size(); i++) {
        LightCycle cycle = cycles.get(i);
        if (cycle.isDead()) {
          continue;
        }
        AI ai = ais.get(i);
        ai.updateDataLocalRunner(ais.size(), i, cyclesToData());
        Move move = ai.makeDecision();
        if (!applyMove(cycle, move)) {
          System.err.println(ai.name + " make invalid move " + move);
          cycle.kill();
          if (gameFinished()) {
            break;
          }
        }
        gameBoard.update(cycles);
      }
    }

    System.err.println(gameBoard);
    AI winner = ais.get(cycles.stream().filter(cycle -> !cycle.isDead()).findFirst().get().index);
    System.err.println("WINNER IS " + winner.name);
  }

  private boolean gameFinished() {
    int alive = 0;
    for (LightCycle cycle : cycles) {
      if (!cycle.isDead()){
        alive++;
      }
    }
    return alive <= 1;
  }

  private boolean applyMove(LightCycle cycle, Move move) {
    Point head = cycle.head;
    Point target = null;
    switch (move) {
      case LEFT:
        target = gameBoard.getPoint(head.x - 1, head.y);
        break;
      case UP:
        target = gameBoard.getPoint(head.x, head.y - 1);
        break;
      case RIGHT:
        target = gameBoard.getPoint(head.x + 1, head.y);
        break;
      case DOWN:
        target = gameBoard.getPoint(head.x, head.y + 1);
        break;
    }
    if (target == null || target.holder != -1) {
      return false;
    }
    cycle.setHead(target);
    return true;
  }

  private int[][] cyclesToData() {
    int[][] res = new int[cycles.size()][4];
    for (int i = 0; i < cycles.size(); i++) {
      LightCycle cycle = cycles.get(i);
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
    runner.setStartPositions(new Point(0,0), new Point(12, 15));
    runner.start();
  }
}
