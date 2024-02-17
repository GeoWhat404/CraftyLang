package me.geowhat.craftylang.interpreter.preprocessor;

import me.geowhat.craftylang.client.CraftyLangClient;
import me.geowhat.craftylang.client.util.Message;
import me.geowhat.craftylang.interpreter.CraftScript;
import me.geowhat.craftylang.interpreter.Modules;
import me.geowhat.craftylang.interpreter.error.ModuleError;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ModuleLoader {

    private final String moduleName;

    public ModuleLoader(String moduleName) {
        this.moduleName = moduleName;
        load();
    }

    private void load() {
        StringBuilder builder = new StringBuilder();
        boolean foundModule = false;

        if (Minecraft.getInstance().player == null)
            return;

        List<ItemStack> playerItems = Minecraft.getInstance().player.inventoryMenu.getItems();
        for (ItemStack item : playerItems) {
            if (!(item.getItem() instanceof WritableBookItem)) {
                continue;
            }

            if (!item.getHoverName().getString().equals(moduleName)) {
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
                foundModule = true;
            }
        }

        if (!foundModule) {
            File dir = new File(CraftyLangClient.SOURCE_FILES);
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file.getName().endsWith(".crs") && file.getName().replace(".crs", "").equals(moduleName)) {
                    try (FileReader reader = new FileReader(file);
                         BufferedReader bufferedReader = new BufferedReader(reader)) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            builder.append(line).append(System.lineSeparator());
                        }
                        foundModule = true;
                    } catch (IOException ignored) {

                    }
                }
            }
        }

        if (Modules.get(moduleName) != null) {
            return;
        }

        StringBuilder tmp = builder;
        builder = new StringBuilder();

        if (foundModule) {
            builder.append(new Preprocessor(tmp.toString()));
            Modules.define(moduleName, builder.toString());
            Message.sendDebug("Found module with name: " + moduleName);
        }

        else
            throw moduleError(moduleName, "Error resolving module " + moduleName);
    }

    @Override
    public String toString() {
        return Modules.get(moduleName);
    }

    private static ModuleError moduleError(String module, String message) {
        ModuleError err = new ModuleError(module, message);
        CraftScript.moduleError(err);
        return err;
    }
}
