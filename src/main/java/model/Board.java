package model;

import java.util.*;

public class Board {

  final int WIDTH, HEIGHT;

  private Map<Pair, Point> allPoints;

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

  public void update(List<LightCycle> cycles) {
    cycles.forEach(cycle -> {
      if (cycle.isDead()) {
        allPoints.values().stream()
            .filter(point -> point.holder == cycle.index)
            .forEach(point -> point.holder = -1);
      } else {
        allPoints.get(cycle.currentPoint.pair).holder = cycle.index;
      }
    });
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
    return availableEmptyPoints(cycle);
  }

  public Move bestMove(LightCycle cycle) {
    Queue<MovePick> variants = new PriorityQueue<>();
    Point curr = cycle.currentPoint;
    for (Point point : availableMoves(cycle)) {
      //apply
      cycle.addHoldedPosition(point);
      variants.add(new MovePick(Move.byPoints(curr, point), evaluate(cycle)));
      //undo
      point.holder = -1;
      cycle.currentPoint = curr;
    }
    return variants.poll().move;
  }

}
