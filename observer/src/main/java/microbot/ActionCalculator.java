package microbot;

import model.engine.Position;
import model.engine.GameObject;
import model.engine.PlayerAction;
import processor.BotProcessor;

public class ActionCalculator {

    private PlayerAction playerAction;
    private int prio;

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

    protected double getDistanceBetween(GameObject object1, int x, int y) {
        var triangleY = Math.abs(object1.getPosition().y - y);
        var triangleX = Math.abs(object1.getPosition().x - x);
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

    protected double clampDouble(double value, double min, double max){
        value = value > max ? max : value;
        value = value < min ? min : value;

        return value;
    }

    /***
     * 
     * @param value nilai dari 0 ke 1
     * @return map nilai easeIn 0 < y < 1
     */
    protected double easeIn(double value){
        return 1 - Math.cos((value * Math.PI) / 2);
    }

    /***
     * 
     * @param value nilai dari 0 ke 1
     * @return map nilai easeOut 0 < y < 1
     */
    protected double easeOut(double value){
        return Math.sin((value * Math.PI) / 2);
    }

    /***
     * 
     * @param value nilai dari 0 ke 1
     * @return map nilai easeInOut 0 < y < 1
     */
    protected double easeInOut(double value){
        return -(Math.cos(Math.PI * value) - 1) / 2;
    }

    protected int lerpInt(float value, int a, int b){
        return (int)(clampFloat(value, 0, 1) * (b - a) + a);
    }

    protected int lerpInt(double value, int a, int b){
        return (int)(clampDouble(value, 0, 1) * (b - a) + a);
    }
    
    protected boolean isInRadius(GameObject object, GameObject target, double radius) {
        return getDistanceBetween(object, target) <= radius;
    }

    protected boolean isInRadius(GameObject object, Position target, double radius) {
        return getDistanceBetween(object, target) <= radius;
    }

    protected boolean isInRadius(GameObject object, int x, int y, double radius) {
        return getDistanceBetween(object, x, y) <= radius;
    }

    protected void sendMessage(PlayerAction playerAction, int prio) {
        if (this.prio > prio) {
            return;
        }
        this.playerAction = playerAction;
        this.prio = prio;
    }

    protected void signalDone(BotProcessor botProcessor) {
        botProcessor.sendMessage(playerAction, prio);
        prio = -1;
    }
    
}
