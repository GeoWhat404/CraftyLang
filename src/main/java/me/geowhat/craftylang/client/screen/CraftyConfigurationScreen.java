package me.geowhat.craftylang.client.screen;

import me.geowhat.craftylang.client.CraftyLangSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class CraftyConfigurationScreen extends Screen {

    private static final String title = "CraftyLang Configuration Menu";

    private final Screen parent;

    private final int maxWhileLimit = 16384;
    private final int maxFunctionArgs = 2048;

    private AbstractSliderButton whileLoopLimitSlider;
    private AbstractSliderButton functionArgsLimitSlider;
    private Button debugModeToggleButton;

    private Button colorSchemeConfigurationButton;
    private Button doneButton;

    private final int baseWidth = 200;
    private final int baseHeight = 20;
    private final int basePadding = 4;

    public CraftyConfigurationScreen(Screen parent) {
        super(Component.literal(title));

        this.parent = parent;
    }

    private double calculateValue(AbstractSliderButton slider, double value) {
        return value * (slider.getWidth() - 8) + slider.getX() + 4;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        debugModeToggleButton = Button.builder(Component.literal("Debug mode: " + CraftyLangSettings.DEBUG_MODE), button -> {
                    CraftyLangSettings.DEBUG_MODE = !CraftyLangSettings.DEBUG_MODE;
                    debugModeToggleButton.setMessage(Component.literal("Debug mode: " + CraftyLangSettings.DEBUG_MODE));
                })
                .bounds(width / 2 - baseWidth / 2, height / 4, baseWidth, baseHeight)
                .tooltip(Tooltip.create(Component.literal("Enable debug messages")))
                .build();

        whileLoopLimitSlider = new AbstractSliderButton(width / 2 - baseWidth / 2, height / 4 + baseHeight + basePadding, baseWidth, baseHeight, CommonComponents.EMPTY, (double) CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS / maxWhileLimit) {

            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal(String.valueOf(CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS)));
            }

            @Override
            protected void applyValue() {
                CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS = (int) Math.floor(Mth.clampedLerp(0.0, maxWhileLimit, this.value));
            }
        };
        whileLoopLimitSlider.setTooltip(Tooltip.create(Component.literal("Adjust the maximum loop iteration (made to not crash your game)")));

        functionArgsLimitSlider = new AbstractSliderButton(width / 2 - baseWidth / 2, height / 4 + 2 * (baseHeight + basePadding), baseWidth, baseHeight, CommonComponents.EMPTY, (double) CraftyLangSettings.MAX_FUNCTION_ARGS / maxFunctionArgs) {

            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal(String.valueOf(CraftyLangSettings.MAX_FUNCTION_ARGS)));
            }

            @Override
            protected void applyValue() {
                CraftyLangSettings.MAX_FUNCTION_ARGS = (int) Math.floor(Mth.clampedLerp(0.0, maxFunctionArgs, this.value));
            }
        };
        functionArgsLimitSlider.setTooltip(Tooltip.create(Component.literal("Maximum number of arguments in a function call")));

        colorSchemeConfigurationButton = Button.builder(Component.literal("Configure color scheme..."), button -> {
            minecraft.setScreen(new ColorSchemeEditScreen(this));
        })
        .bounds(width / 2 - baseWidth / 2, height / 4 + 3 * (baseHeight + basePadding), baseWidth, baseHeight)
        .build();

        doneButton = Button.builder(CommonComponents.GUI_DONE, button -> {
            minecraft.setScreen(parent);
        })
        .bounds(width / 2 - baseWidth / 2, height / 4 + 4 * (baseHeight + basePadding), baseWidth, baseHeight)
        .build();

        // set the correct value (cursed++)
        whileLoopLimitSlider.onClick(calculateValue(whileLoopLimitSlider, (double) CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS / maxWhileLimit), 0);
        functionArgsLimitSlider.onClick(calculateValue(whileLoopLimitSlider, (double) CraftyLangSettings.MAX_FUNCTION_ARGS / maxFunctionArgs), 0);

        addRenderableWidget(debugModeToggleButton);
        addRenderableWidget(whileLoopLimitSlider);
        addRenderableWidget(functionArgsLimitSlider);
        addRenderableWidget(colorSchemeConfigurationButton);
        addRenderableWidget(doneButton);
    }
}
