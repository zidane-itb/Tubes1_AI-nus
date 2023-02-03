package bot.imp;

import bot.ActionBot;
import enums.Action;
import processor.MainProcessor;

import java.util.concurrent.TimeUnit;

public class MoveBot implements ActionBot {

    private final MainProcessor mainProcessor;

    public MoveBot(MainProcessor mainProcessor) {
        this.mainProcessor = mainProcessor;
    }

    public void run(String condition) throws InterruptedException {
        int min = 50;
        int max = 100;
        int random_int = (int)Math.floor(Math.random() * (max - min + 1) + min);
        TimeUnit.MILLISECONDS.sleep(100);
        mainProcessor.sendMessage(Action.MOVE, random_int);
       // System.out.println("move bot is executed.");

    }
}
