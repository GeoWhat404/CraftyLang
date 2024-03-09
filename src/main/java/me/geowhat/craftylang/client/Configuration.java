package me.geowhat.craftylang.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.geowhat.craftylang.client.util.Message;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;

public class Configuration {

    private boolean debugMode;
    private int maxWhileLoopIteration;
    private int maxFunctionArgs;
    private boolean enableSyntaxHighlighting;
    private String colorScheme;

    private static boolean configExists(String filepath) {
        return new File(filepath).exists();
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public static Configuration fromJson(String filepath) throws IOException {
        if (!configExists(filepath)) {
            CraftyLangClient.firstLoad = true;
            CraftyLangClient.logger.info("Creating new config file");
            return new Configuration();
        }

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filepath)) {
            return gson.fromJson(reader, Configuration.class);
        } catch (IOException e) {
            CraftyLangClient.logger.error(e);
            return new Configuration();
        }
    }

    public void saveJsonFile(String filepath) {
        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(toJson());
        } catch (IOException e) {
            CraftyLangClient.logger.error(e);
        }
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void setMaxWhileLoopIteration(int limit) {
        maxWhileLoopIteration = limit;
    }

    public int getMaxWhileLoopIteration() {
        return maxWhileLoopIteration;
    }

    public int getMaxFunctionArgs() {
        return maxFunctionArgs;
    }

    public void setMaxFunctionArgs(int max) {
        this.maxFunctionArgs = max;
    }

    public boolean isEnableSyntaxHighlighting() {
        return enableSyntaxHighlighting;
    }

    public void setEnableSyntaxHighlighting(boolean enableSyntaxHighlighting) {
        this.enableSyntaxHighlighting = enableSyntaxHighlighting;
    }

    public String getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(String colorScheme) {
        this.colorScheme = colorScheme;
    }
}
