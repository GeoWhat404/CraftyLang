package me.geowhat.craftylang.client.util;

import me.geowhat.craftylang.client.CraftyLangClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Exporter {

    public static void export(String filename, List<String> contents) {
        File file = new File(CraftyLangClient.SOURCE_FILES + filename + ".crs");
        try {
            file.createNewFile();

            FileWriter writer = new FileWriter(file);
            for (String line : contents) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
            writer.close();

            Message.sendSuccess("Successfully exported to \"" + filename + ".crs\"");
        } catch (IOException e) {
            Message.sendError("Could not export the code: " + e.getMessage());
        }
    }
}
