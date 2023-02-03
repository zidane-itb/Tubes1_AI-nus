package processor;

import bot.ActionBot;
import bot.imp.MoveBot;
import bot.imp.ShootBot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainProcessor {

    private final ExecutorService executor;
    private final ActionBot[] actionBots;

    public MainProcessor(ExecutorService executor, ActionBot[] actionBots) {
        this.executor = executor;
        this.actionBots = actionBots;
    }

    public void execute() {
        if (executor == null) return;
        if (actionBots == null) return;

    }

}
