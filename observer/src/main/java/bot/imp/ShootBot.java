package bot.imp;

import bot.ActionBot;
import enums.Action;
import processor.MainProcessor;

import java.util.concurrent.TimeUnit;

public class ShootBot implements ActionBot {

    private final MainProcessor mainProcessor;

    public ShootBot(MainProcessor mainProcessor) {
        this.mainProcessor = mainProcessor;
    }

    public void run(String condition) throws InterruptedException {
        int min = 50; // Minimum value of range
        int max = 100; // Maximum value of range
        int random_int = (int)Math.floor(Math.random() * (max - min + 1) + min);
        TimeUnit.MILLISECONDS.sleep(100);
        mainProcessor.sendMessage(Action.SHOOT, random_int);
      //  System.out.println("shoot bot is executed.");
    }
}
