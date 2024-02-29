package me.geowhat.craftylang.client;

import me.geowhat.craftylang.interpreter.CraftScript;
import me.geowhat.craftylang.interpreter.Keywords;
import me.geowhat.craftylang.interpreter.syntax.ColorScheme;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CraftyLangClient implements ClientModInitializer {

    public static final String VERSION = "v1.0.0 alpha";
    public static final String MODID = "craftylang";
    public static final String CRAFTY_PATH = "./config/crafty/";
    public static final String SOURCE_FILES = CRAFTY_PATH + "source/";

    public static Map<String, ColorScheme> colorSchemes = new HashMap<>();

    public static Logger logger = LogManager.getLogger(MODID);

    private final String configFile = CRAFTY_PATH + "craftyconfig.json";
    private Configuration config;

    private void addColorScheme(String name) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("assets/craftylang/themes/" + name + ".json");
        colorSchemes.put(name, ColorScheme.fromJson(is).setName(name));
    }

    @Override
    public void onInitializeClient() {
        logger.info("CraftyLang started");

        try {
            addColorScheme("default");
            addColorScheme("craftscript");
            addColorScheme("gruvbox_dark");
            addColorScheme("gruvbox_light");
        } catch (NullPointerException | IOException e) {
            logger.error("Failed to load ColorSchemes");
        }

        CraftScript.init();
        Keywords.addKeywords();
        KeyBindings.register();

        try {
            Files.createDirectories(Paths.get(CRAFTY_PATH));
            Files.createDirectories(Paths.get(SOURCE_FILES));

        } catch (IOException e) {
            logger.error(e);
        }

        logger.info("Loading config file");
        try {
            config = Configuration.fromJson(configFile);

            CraftyLangSettings.DEBUG_MODE = config.isDebugMode();
            CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS = config.getMaxWhileLoopIteration();
            CraftyLangSettings.MAX_FUNCTION_ARGS = config.getMaxFunctionArgs();
            CraftyLangSettings.ENABLE_SYNTAX_HIGHLIGHTING = config.isEnableSyntaxHighlighting();
            CraftyLangSettings.COLOR_SCHEME = colorSchemes.get(config.getColorScheme());

        } catch (IOException e) {
            logger.error("Config file not found, creating a new one");
            config = new Configuration();
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register(event -> {
            CraftScript.executorService.shutdown();

            config.setDebugMode(CraftyLangSettings.DEBUG_MODE);
            config.setMaxWhileLoopIteration(CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS);
            config.setMaxFunctionArgs(CraftyLangSettings.MAX_FUNCTION_ARGS);
            config.setEnableSyntaxHighlighting(CraftyLangSettings.ENABLE_SYNTAX_HIGHLIGHTING);
            config.setColorScheme(CraftyLangSettings.COLOR_SCHEME.getName());
            config.saveJsonFile(configFile);
        });
    }
}
