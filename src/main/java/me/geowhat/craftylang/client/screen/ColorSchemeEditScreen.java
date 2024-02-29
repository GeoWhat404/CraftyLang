package me.geowhat.craftylang.client.screen;

import me.geowhat.craftylang.client.CraftyLangClient;
import me.geowhat.craftylang.client.CraftyLangSettings;
import me.geowhat.craftylang.client.util.Message;
import me.geowhat.craftylang.interpreter.syntax.ColorScheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ColorSchemeEditScreen extends Screen {

    private final Screen parent;

    private Button toggleColorSchemeButton;
    private Button switchColorSchemeButton;
    private Button doneButton;

    private final int baseWidth = 200;
    private final int baseHeight = 20;
    private final int basePadding = 4;

    protected ColorSchemeEditScreen(Screen parent) {
        super(Component.literal("Color Scheme edit screen"));

        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void init() {
        toggleColorSchemeButton = Button.builder(Component.literal((CraftyLangSettings.ENABLE_SYNTAX_HIGHLIGHTING ? "Disable" : "Enable") + " Syntax highlighting"), button -> {
            toggleColorSchemeButton.setMessage(Component.literal((CraftyLangSettings.ENABLE_SYNTAX_HIGHLIGHTING ? "Enable" : "Disable") + " Syntax highlighting"));
            CraftyLangSettings.ENABLE_SYNTAX_HIGHLIGHTING = !CraftyLangSettings.ENABLE_SYNTAX_HIGHLIGHTING;
        })
        .bounds(width / 2 - baseWidth / 2, height / 4, baseWidth, baseHeight)
        .build();

        switchColorSchemeButton = Button.builder(Component.literal("Current color scheme: " + CraftyLangSettings.COLOR_SCHEME.getName()), button -> {
            Set<String> colorSchemes = CraftyLangClient.colorSchemes.keySet();
            String[] buffer = colorSchemes.toArray(new String[0]);

            int sIdx = 0;
            int idx = 0;
            Iterator<String> iter = colorSchemes.iterator();
            while (iter.hasNext()) {
                String current = iter.next();
                if (current.equals(CraftyLangSettings.COLOR_SCHEME.getName())) {
                    sIdx = idx;
                    break;
                }
                idx++;
            }

            idx = sIdx;
            do {
                String current = buffer[idx];

                CraftyLangSettings.COLOR_SCHEME = CraftyLangClient.colorSchemes.get(current);
                switchColorSchemeButton.setMessage(Component.literal("Current color scheme: " + CraftyLangSettings.COLOR_SCHEME.getName()));

                idx = (idx + 1) % buffer.length;
            } while (idx != sIdx);
        })
        .bounds(width / 2 - baseWidth / 2, height / 4 + baseHeight + basePadding, baseWidth, baseHeight)
        .build();

        doneButton = Button.builder(CommonComponents.GUI_DONE, button -> {
            minecraft.setScreen(parent);
        })
        .bounds(width / 2 - baseWidth / 2, height / 4 + 2 * (baseHeight + basePadding), baseWidth, baseHeight)
        .build();

        addRenderableWidget(toggleColorSchemeButton);
        addRenderableWidget(switchColorSchemeButton);
        addRenderableWidget(doneButton);
    }
}
