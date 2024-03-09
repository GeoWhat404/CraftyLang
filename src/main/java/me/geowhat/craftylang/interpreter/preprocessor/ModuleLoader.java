package me.geowhat.craftylang.interpreter.preprocessor;

import me.geowhat.craftylang.client.util.Message;
import me.geowhat.craftylang.interpreter.CraftScript;
import me.geowhat.craftylang.interpreter.Modules;
import me.geowhat.craftylang.interpreter.error.ModuleError;
import net.minecraft.client.Minecraft;

public class ModuleLoader {

    private final String moduleName;

    public ModuleLoader(String moduleName) {
        this.moduleName = moduleName;
        load();
    }

    private void load() {

        if (Minecraft.getInstance().player == null)
            return;

        Modules.reload();

        if (!Modules.isDefined(moduleName)) {
            throw moduleError(moduleName, "Error resolving module " + moduleName);
        }
        Message.sendDebug("Loading module: " + moduleName);
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
