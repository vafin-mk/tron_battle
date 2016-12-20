package model;

import ai.AI;

import java.util.*;

public class Board {

  final int WIDTH, HEIGHT;

  private Map<Pair, Point> allPoints;
  private LightCycle closestEnemy;
  private LightCycle myCycle;

  public Board(int width, int height) {
    this.WIDTH = width;
    this.HEIGHT = height;
    this.allPoints = new HashMap<>(WIDTH * HEIGHT);
    for (int x = 0; x < WIDTH; x++) {
      for (int y = 0; y < HEIGHT; y++) {
        allPoints.put(new Pair(x, y), new Point(x, y));
      }
    }
  }

  public void update(List<LightCycle> cycles, LightCycle myCycle) {
    int closestDist = 100000;
    LightCycle closest = null;
    for (LightCycle cycle : cycles) {
      if (cycle.isDead()) {
        allPoints.values().stream()
            .filter(point -> point.holder == cycle.index)
            .forEach(point -> point.holder = -1);
      } else {
        allPoints.get(cycle.currentPoint.pair).holder = cycle.index;
        if (!cycle.me) {
          int dist = cycle.currentPoint.manhattanDist(myCycle.currentPoint);
          if (dist < closestDist) {
            closestDist = dist;
            closest = cycle;
          }
        }
      }
    }
    closestEnemy = closest;
    this.myCycle = myCycle;
  }

  public List<Point> availableMoves(LightCycle cycle) {
    List<Point> res = new ArrayList<>();
    Point from = cycle.currentPoint;
    Point target = allPoints.get(from.pair.neighbour(Move.LEFT));
    if (target != null && target.holder == -1) {
      res.add(target);
    }
    target = allPoints.get(from.pair.neighbour(Move.UP));
    if (target != null && target.holder == -1) {
      res.add(target);
    }
    target = allPoints.get(from.pair.neighbour(Move.RIGHT));
    if (target != null && target.holder == -1) {
      res.add(target);
    }
    target = allPoints.get(from.pair.neighbour(Move.DOWN));
    if (target != null && target.holder == -1) {
      res.add(target);
    }
    return res;
  }

  public int availableEmptyPoints(LightCycle cycle) {
    Point start = cycle.currentPoint;
    List<Point> res = new ArrayList<>();
    addAvailablePoint(allPoints.get(start.pair.neighbour(Move.LEFT)), res);
    addAvailablePoint(allPoints.get(start.pair.neighbour(Move.UP)), res);
    addAvailablePoint(allPoints.get(start.pair.neighbour(Move.RIGHT)), res);
    addAvailablePoint(allPoints.get(start.pair.neighbour(Move.DOWN)), res);
    return res.size();
  }

  private void addAvailablePoint(Point point, List<Point> points) {
    if (point == null || point.holder != -1 || points.contains(point)) {
      return;
    }
    points.add(point);
    addAvailablePoint(allPoints.get(point.pair.neighbour(Move.LEFT)), points);
    addAvailablePoint(allPoints.get(point.pair.neighbour(Move.UP)), points);
    addAvailablePoint(allPoints.get(point.pair.neighbour(Move.RIGHT)), points);
    addAvailablePoint(allPoints.get(point.pair.neighbour(Move.DOWN)), points);
  }

  public int evaluate(LightCycle cycle) {
    return cycle.me ? availableEmptyPoints(cycle) : -availableEmptyPoints(cycle);
  }

  public Move bestMove() {
    return minimax(AI.PREDICTION_DEPTH, myCycle, new int[] {Integer.MIN_VALUE}, new int[] {Integer.MAX_VALUE}).move;
  }

  //minimax with alpha/beta pruning
  //https://www.ntu.edu.sg/home/ehchua/programming/java/JavaGame_TicTacToe_AI.html
  private MovePick minimax(int depth, LightCycle cycle, int[] alpha, int[] beta) {
    System.err.println("DEPTH:" + depth + "|alpha=" + alpha[0] + "|beta=" + beta[0]);
    Point curr = cycle.currentPoint;
    List<Point> available = availableMoves(cycle);
    Point best = null;
    if (available.isEmpty() || depth == 0) {
      int score = evaluate(cycle);
      return new MovePick(Move.UP, score);
    } else {
      for (Point point : available) {
        if (best == null) {
          best = point;
        }
        //apply
        cycle.addHoldedPosition(point);

        if (cycle.me) {//maximize
          int score = minimax(depth - 1, closestEnemy, alpha, beta).priority;
          if (score > alpha[0]) {
            alpha[0] = score;
            best = point;
          }
        } else {//minimize
          int score = minimax(depth - 1, myCycle, alpha, beta).priority;
          if (score < beta[0]) {
            beta[0] = score;
            best = point;
          }
        }
        //undo
        point.holder = -1;
        cycle.currentPoint = curr;

        if (alpha[0] >= beta[0]) {
          break;
        }
      }
      return new MovePick(Move.byPoints(curr, best), cycle.me ? alpha[0] : beta[0]);
    }
  }

}
