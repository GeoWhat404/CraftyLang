package me.geowhat.craftylang.interpreter;

import me.geowhat.craftylang.client.CraftyLangClient;
import me.geowhat.craftylang.client.util.Message;
import me.geowhat.craftylang.crs.CRSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Modules {

    public static Map<String, String> modules = new HashMap<>();

    public static void define(String name, String code) {
        Message.sendDebug("Defined module: " + name + ", code: \n" + code);
        modules.put(name, code);
    }

    public static String get(String module) {
        return modules.get(module);
    }

    public static boolean isDefined(String name) {
        return modules.containsKey(name);
    }

    public static void reload() {
        modules.clear();

        // BUILT-IN MODULES
        define("math", CRSMath.code);

        StringBuilder builder;

        // FILES
        File dir = new File(CraftyLangClient.SOURCE_FILES);

        if (dir.listFiles() == null) {
            return;
        }

        for (File file : Objects.requireNonNull(dir.listFiles())) {
            builder = new StringBuilder();
            if (file.getName().endsWith(".crs")) {
                try {
                    FileReader reader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line).append(System.lineSeparator());
                    }
                    define(file.getName().replace(".crs", ""), builder.toString());
                } catch (IOException ignored) {
                }
            }
        }

        // BOOKS
        if (Minecraft.getInstance().player == null) return;

        List<ItemStack> playerItems = Minecraft.getInstance().player.inventoryMenu.getItems();
        for (ItemStack item : playerItems) {

            builder = new StringBuilder();
            if (!(item.getItem() instanceof WritableBookItem)) {
                continue;
            }

            CompoundTag bookTag = item.getTag();
            if (bookTag != null && bookTag.contains("pages")) {
                ListTag pagesTag = bookTag.getList("pages", 8);
                StringBuilder pages = new StringBuilder();
                for (int k = 0; k < pagesTag.size(); k++) {
                    pages.append(pagesTag.getString(k));
                }
                builder.append(pages);
                define(item.getHoverName().getString(), builder.toString());
            }
        }

        printDefinitions();
    }

    public static void printDefinitions() {
        Message.sendDebug("MODULE DEFINITIONS: " + modules.size());
        for (Map.Entry<String, String> entry : modules.entrySet()) {
            if (!entry.getKey().equals("math")) {
                Message.sendDebug("NAME: " + entry.getKey() + " | VALUE: \n" + entry.getValue());
                Message.sendNewline();
            }
        }
    }
}
