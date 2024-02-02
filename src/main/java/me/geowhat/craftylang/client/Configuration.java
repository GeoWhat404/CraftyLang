package me.geowhat.craftylang.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class Configuration {

    private int maxWhileLoopIteration;

    private int maxFunctionArgs;

    private static boolean configExists(String filepath) {
        return new File(filepath).exists();
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public static Configuration fromJson(String filepath) throws IOException {
        if (!configExists(filepath)) {
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
}
