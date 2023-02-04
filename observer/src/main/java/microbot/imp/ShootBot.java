package microbot.imp;

import etc.StateHolder;
import lombok.RequiredArgsConstructor;
import microbot.ActionBot;
import microbot.ActionCalculator;
import model.engine.PlayerAction;
import processor.BotProcessor;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ShootBot extends ActionCalculator implements ActionBot {

    private final BotProcessor botProcessor;
    private final StateHolder stateHolder;

    public void run() throws InterruptedException {
        int min = 50; // Minimum value of range
        int max = 100; // Maximum value of range
        int random_int = (int)Math.floor(Math.random() * (max - min + 1) + min);
        TimeUnit.MILLISECONDS.sleep(100);
        botProcessor.sendMessage(new PlayerAction(), 0);
      //  System.out.println("shoot bot is executed.");
    }
}
