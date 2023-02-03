import bot.ActionBot;
import bot.imp.MoveBot;
import bot.imp.ShootBot;
import processor.MainProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static ActionBot[] actionBots;
    private static ExecutorService executor;

    public static void main(String[] args) throws InterruptedException {
        executor = Executors.newFixedThreadPool( 2 );

        MainProcessor mainProcessor = new MainProcessor();

        actionBots = new ActionBot[]{new MoveBot(mainProcessor), new ShootBot(mainProcessor)};
        whileVer();

    }

    private static void whileVer() throws InterruptedException {
        while (true)
            bot();
    }

    private static void runnableVer() {
        Runnable helloRunnable = () -> {
            try {
                bot();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleAtFixedRate(helloRunnable, 0, 100, TimeUnit.MILLISECONDS);
    }

    private static void bot() throws InterruptedException {
        for (ActionBot actionBot : actionBots) {
            executor.execute(() -> {
                try {
                    actionBot.run("condition");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }


}
