package microbot;

// import javax.swing.text.Position; why import these? auto imports?

import model.engine.Position;
import model.engine.GameObject;

public class ActionCalculator {

    protected double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    protected double getDistanceBetween(GameObject object1, Position object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.x);
        var triangleY = Math.abs(object1.getPosition().y - object2.y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    protected int getHeadingBetween(GameObject bot, GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    protected int getHeadingBetween(GameObject bot, Position otherPosition ) {
        var direction = toDegrees(Math.atan2(otherPosition.y - bot.getPosition().y,
                otherPosition.x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    protected int rotateHeadingBy(int heading, int rotateAmount){
        heading += rotateAmount;

        heading = heading < 0 ? heading + 360 : heading;
        heading = heading >= 360 ? heading - 360 : heading;

        return heading;
    }

    protected int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    protected int clampInt(int value, int min, int max){
        value = value > max ? max : value;
        value = value < min ? min : value;

        return value;
    }

    protected float clampFloat(float value, float min, float max){
        value = value > max ? max : value;
        value = value < min ? min : value;

        return value;
    }

    protected int lerpInt(float value, int a, int b){
        return (int)(clampFloat(value, 0, 1) * (b - a) + a);
    }
    
}
