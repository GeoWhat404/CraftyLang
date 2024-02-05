package me.geowhat.craftylang.client;

import me.geowhat.craftylang.interpreter.CraftScript;
import me.geowhat.craftylang.interpreter.Keywords;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class CraftyLangClient implements ClientModInitializer {

    public static final String VERSION = "v1.0.0 alpha";
    public static final String MODID = "craftylang";
    public static Logger logger = LogManager.getLogger(MODID);

    private final String configFile = "./config/craftyconfig.json";
    private Configuration config;

    @Override
    public void onInitializeClient() {
        logger.info("CraftyLang started");

        CraftScript.init();
        Keywords.addKeywords();
        KeyBindings.register();

        logger.info("Loading config file");
        try {
            config = Configuration.fromJson(configFile);

            CraftyLangSettings.DEBUG_MODE = config.isDebugMode();
            CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS = config.getMaxWhileLoopIteration();
            CraftyLangSettings.MAX_FUNCTION_ARGS = config.getMaxFunctionArgs();

        } catch (IOException e) {
            logger.error("Config file not found, creating a new one");
            config = new Configuration();
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register(event -> {
            CraftScript.executorService.shutdown();

            config.setDebugMode(CraftyLangSettings.DEBUG_MODE);
            config.setMaxWhileLoopIteration(CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS);
            config.setMaxFunctionArgs(CraftyLangSettings.MAX_FUNCTION_ARGS);
            config.saveJsonFile(configFile);
        });
    }
}
