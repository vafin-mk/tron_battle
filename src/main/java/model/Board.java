package model;

import ai.AI;

import java.util.*;

public class Board {

  public static final int EMPTY = -1;

  final int WIDTH, HEIGHT;

  private Point[][] _board;
  private LightCycle closestEnemy;
  private LightCycle myCycle;

  public Board(int width, int height) {
    this.WIDTH = width;
    this.HEIGHT = height;
    this._board = new Point[WIDTH][HEIGHT];
    for (int x = 0; x < WIDTH; x++) {
      for (int y = 0; y < HEIGHT; y++) {
        _board[x][y] = new Point(x, y);
      }
    }
  }

  public Point getPoint(int x, int y) {
    if (x < 0 || x > WIDTH - 1 || y < 0 || y > HEIGHT - 1) {
      return null;
    }
    return _board[x][y];
  }

  public void update(List<LightCycle> cycles) {
    cycles.stream().filter(LightCycle::isDead).forEach(cycle -> {
      for (Point[] row : _board) {
        for (Point point : row) {
          if (point.holder == cycle.index) {
            point.holder = EMPTY;
          }
        }
      }
    });
  }

  public List<Point> availableMoves(LightCycle cycle) {
    List<Point> res = new ArrayList<>();
    Point from = cycle.head;
    floodFill(from, res, 1);
    return res;
  }

  public List<Point> findAvailablePoints(LightCycle cycle, int range) {
    Point start = cycle.head;
    List<Point> res = new ArrayList<>();
    floodFill(start, res, range);
    return res;
  }

  private void addAvailablePoint(Point point, List<Point> points, int maxRange) {
    if (point == null || point.holder != -1 || points.contains(point) || maxRange == 0) {
      return;
    }
    points.add(point);
    floodFill(point, points, maxRange - 1);
  }

  private void floodFill(Point point, List<Point> points, int range) {
    addAvailablePoint(getPoint(point.x - 1, point.y), points, range);//left
    addAvailablePoint(getPoint(point.x, point.y - 1), points, range);//up
    addAvailablePoint(getPoint(point.x + 1, point.y), points, range);//right
    addAvailablePoint(getPoint(point.x, point.y + 1), points, range);//down
  }

  public int evaluate(LightCycle cycle) {
    return findAvailablePoints(cycle, 100).size();
  }

  public Move bestMove() {
    //// FIXME: 21.12.16 minimax alpha beta cutoff doesn't work
    return minimax(AI.PREDICTION_DEPTH, myCycle, new int[] {Integer.MIN_VALUE}, new int[] {Integer.MAX_VALUE}).move;

  }

  //minimax with alpha/beta pruning
  //https://www.ntu.edu.sg/home/ehchua/programming/java/JavaGame_TicTacToe_AI.html
  private MovePick minimax(int depth, LightCycle cycle, int[] alpha, int[] beta) {
    Point curr = cycle.head;
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
        cycle.setHead(point);

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
        cycle.setHead(curr);

        if (alpha[0] >= beta[0]) {
          break;
        }
      }
      return new MovePick(Move.byPoints(curr, best), cycle.me ? alpha[0] : beta[0]);
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        builder.append("|");
        switch (_board[x][y].holder) {
          case -1:
            builder.append(" ");
            break;
          case 0:
            builder.append("!");
            break;
          case 1:
            builder.append("?");
            break;
          case 2:
            builder.append("&");
            break;
          case 3:
            builder.append("%");
            break;
          default:
            builder.append("#");
            break;
        }
      }
      builder.append("|\n");
    }
    return builder.toString();
  }
}
