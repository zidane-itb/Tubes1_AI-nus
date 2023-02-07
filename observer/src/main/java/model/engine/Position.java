package model.engine;

import java.util.List;

public class Position {

  public int x;
  public int y;

  public Position() {
    x = 0;
    y = 0;
  }

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /***
   * get centroid of list of positions
   * @param listOfPos 
   */
  public static Position getCentroid(List<Position> listOfPos){
    Position centroid = new Position();

    // need a better way to handle this
    if(listOfPos.isEmpty())
      return centroid;

    listOfPos
      .stream()
      .forEach((myPos) -> {
          centroid.moveXY(myPos.getX(), myPos.getY());
        });
    
    centroid.multiplyBy(1/listOfPos.size());

    return centroid;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public void moveXY(int x, int y){
    this.x += x;
    this.y += y;
  }

  public void multiplyBy(int multiplier){
    this.x *= multiplier;
    this.y *= multiplier;
  }
}
