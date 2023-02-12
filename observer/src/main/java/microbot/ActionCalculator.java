package microbot;

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

    protected int getHeadingBetween(GameObject bot, GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    protected int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    protected boolean isInRadius(GameObject object, GameObject target, double radius) {

        return true;
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
