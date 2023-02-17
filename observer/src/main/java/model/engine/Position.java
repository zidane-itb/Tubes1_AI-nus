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

  // /***
  //  * get centroid of list of positions
  //  * @param listOfPos 
  //  */
  // public static Position getCentroid(List<Position> listOfPos){
  //   Position centroid = new Position();

  //   // need a better way to handle this
  //   if(listOfPos.isEmpty())
  //     return centroid;

  //   listOfPos
  //     .stream()
  //     .forEach((myPos) -> {
  //         centroid.moveXY(myPos.getX(), myPos.getY());
  //         // System.out.println(centroid.x + ", " + centroid.y);
  //       });
    
  //       // System.out.println("veear " + centroid.x + ", " + centroid.y);
  //   // centroid.multiplyBy(1f/listOfPos.size());
  //       centroid.setX(centroid.x/listOfPos.size());
  //       centroid.setY(centroid.y/listOfPos.size());

        

  //   return centroid;
  // }

  /***
   * get centroid of list of positions
   * @param listOfPos 
   */
  public static Position getCentroid(List<GameObject> listOfPos){
    Position centroid = new Position();

    // need a better way to handle this
    if(listOfPos.isEmpty())
      return centroid;

    listOfPos
      .stream()
      .forEach((myPos) -> {
          centroid.moveXY(myPos.getPosition().getX(), myPos.getPosition().getY());
          // System.out.println(centroid.x + ", " + centroid.y);
        });
    
        // System.out.println("veear " + centroid.x + ", " + centroid.y);
    // centroid.multiplyBy(1f/listOfPos.size());
        centroid.setX(centroid.x/listOfPos.size());
        centroid.setY(centroid.y/listOfPos.size());

        

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

  public void multiplyBy(float multiplier){
    this.x = (int) (this.x * multiplier);
    this.y = (int) (this.x * multiplier);
  }
}
