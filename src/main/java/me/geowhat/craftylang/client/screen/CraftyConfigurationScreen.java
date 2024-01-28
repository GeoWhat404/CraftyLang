package me.geowhat.craftylang.client.screen;

import me.geowhat.craftylang.client.CraftyLangSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class CraftyConfigurationScreen extends Screen {

    private static final String title = "CraftyLang Configuration Menu";

    private int maxWhileLimit = 2048;

    private Button enableWhileLoopLimitButton;
    private AbstractSliderButton whileLoopLimitSlider;

    private final int baseWidth = 200;
    private final int baseHeight = 20;

    public CraftyConfigurationScreen() {
        super(Component.literal(title));
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
        enableWhileLoopLimitButton = Button.builder(Component.literal(ConfigScreenSettings.canChangeWhileLoopLimit ? "Disable while loop limit" : "Enable while loop limit"), button -> {
            if (ConfigScreenSettings.canChangeWhileLoopLimit) {
                ConfigScreenSettings.canChangeWhileLoopLimit = false;
                CraftyLangSettings.LIMIT_WHILE_LOOP = false;
                enableWhileLoopLimitButton.setMessage(Component.literal("Enable while loop limit"));
            } else {
                CraftyLangSettings.LIMIT_WHILE_LOOP = true;
                ConfigScreenSettings.canChangeWhileLoopLimit = true;
                enableWhileLoopLimitButton.setMessage(Component.literal("Disable while loop limit"));
            }
            whileLoopLimitSlider.active = ConfigScreenSettings.canChangeWhileLoopLimit;

        }).bounds(width / 2 - baseWidth / 2, 50, baseWidth, baseHeight).build();

        whileLoopLimitSlider = new AbstractSliderButton(width / 2 - baseWidth / 2, 50 + baseHeight + 4, baseWidth, baseHeight, CommonComponents.EMPTY, (double) CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS / maxWhileLimit) {

            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal(String.valueOf(CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS)));
            }

            @Override
            protected void applyValue() {
                CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS = (int) Math.floor(Mth.clampedLerp(0.0, maxWhileLimit, this.value));
            }
        };

        // set the correct value (cursed++)
        whileLoopLimitSlider.onClick(calculateValue(whileLoopLimitSlider, (double) CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS / maxWhileLimit), 0);

        addRenderableWidget(enableWhileLoopLimitButton);
        addRenderableWidget(whileLoopLimitSlider);
    }
}
