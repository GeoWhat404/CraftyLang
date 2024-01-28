package me.geowhat.craftylang.client;

import me.geowhat.craftylang.client.util.Scheduler;
import me.geowhat.craftylang.interpreter.CraftScript;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CraftyLangClient implements ClientModInitializer {

    public static final String VERSION = "v1.0.0 alpha";
    public static final String MODID = "craftylang";
    public static Logger logger = LogManager.getLogger(MODID);

    private final String configFile = "./config/craftyconfig.json";
    private Configuration config;

    @Override
    public void onInitializeClient() {
        logger.info("CraftyLang started");

        KeyBindings.register();

        logger.info("Loading config file");
        try {
            config = Configuration.fromJson(configFile);

            CraftyLangSettings.LIMIT_WHILE_LOOP = config.isLimitWhileLoop();
            CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS = config.getMaxWhileLoopIteration();
            CraftyLangSettings.MAX_FUNCTION_ARGS = config.getMaxFunctionArgs();

        } catch (IOException e) {
            logger.error("Config file not found, creating a new one");
            config = new Configuration();
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register(event -> {
            CraftScript.executorService.shutdown();

            config.setLimitWhileLoop(CraftyLangSettings.LIMIT_WHILE_LOOP);
            config.setMaxWhileLoopIteration(CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS);
            config.setMaxFunctionArgs(CraftyLangSettings.MAX_FUNCTION_ARGS);
            config.saveJsonFile(configFile);
        });
    }
}
