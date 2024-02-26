package me.geowhat.craftylang.mixin;

import it.unimi.dsi.fastutil.ints.IntList;
import me.geowhat.craftylang.client.util.Exporter;
import me.geowhat.craftylang.interpreter.CraftScript;
import me.geowhat.craftylang.interpreter.Keywords;
import me.geowhat.craftylang.interpreter.Modules;
import me.geowhat.craftylang.interpreter.preprocessor.Preprocessor;
import me.geowhat.craftylang.interpreter.syntax.SyntaxColorPalette;
import me.geowhat.craftylang.interpreter.syntax.SyntaxHighlighter;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Objects;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin extends Screen {

    @Unique
    private static final SyntaxHighlighter SYNTAX_HIGHLIGHTER = new SyntaxHighlighter(
            new SyntaxColorPalette(
                    0x797978,
                    0x0099e6,
                    0xc38e22,
                    0xdd713c),
            Keywords.keywords.keySet()
    );

    @Unique private Button interpretButton;
    @Unique private Button saveChangesButton;
    @Unique private Button exportButton;
    @Unique private Button reloadModuleButton;

    @Unique private final int baseButtonWidth = 98;
    @Unique private final int baseButtonHeight = 20;
    @Unique private boolean savedChanges = false;

    protected BookEditScreenMixin(Component component) {
        super(component);
    }

    @Mutable
    @Shadow @Final private List<String> pages;

    @Shadow protected abstract void saveChanges(boolean bl);

    @Shadow private Component pageMsg;

    @Shadow private int currentPage;

    @Shadow protected abstract int getNumPages();

    @Shadow protected abstract void init();

    @Shadow private boolean isModified;

    @Shadow protected abstract void updateButtonVisibility();

    @Shadow protected abstract void clearDisplayCache();

    @Shadow public abstract boolean keyPressed(int i, int j, int k);

    @Shadow @Final private ItemStack book;

    @Shadow protected abstract BookEditScreen.Pos2i convertLocalToScreen(BookEditScreen.Pos2i pos2i);

    @Unique
    private void updatePageMessage() {
        if (!this.savedChanges)
            this.pageMsg = Component.literal("*").append(Component.translatable("book.pageIndicator", this.currentPage + 1, this.getNumPages()));
        else
            this.pageMsg = Component.translatable("book.pageIndicator", this.currentPage + 1, this.getNumPages());
    }

    @Inject(method = "saveChanges", at = @At("HEAD"))
    public void saveChanges(boolean bl, CallbackInfo ci) {
        this.savedChanges = true;
        updatePageMessage();
    }

    @Inject(method = "charTyped", at = @At("HEAD"))
    public void charTyped(char ch, int i, CallbackInfoReturnable<Boolean> cir) {
        this.savedChanges = false;

        updatePageMessage();
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void init(CallbackInfo ci) {
        this.savedChanges = true;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;getDisplayCache()Lnet/minecraft/client/gui/screens/inventory/BookEditScreen$DisplayCache;"))
    public void render(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        updatePageMessage();
    }

    @Inject(method = "changeLine", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;getDisplayCache()Lnet/minecraft/client/gui/screens/inventory/BookEditScreen$DisplayCache;"))
    public void changeLine(int i, CallbackInfo ci) {
        // formation = "";
        updatePageMessage();
    }

    @Inject(method = "keyHome", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;getDisplayCache()Lnet/minecraft/client/gui/screens/inventory/BookEditScreen$DisplayCache;"))
    public void keyHome(CallbackInfo ci) {
        updatePageMessage();
    }

    @Inject(method = "keyEnd", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;getDisplayCache()Lnet/minecraft/client/gui/screens/inventory/BookEditScreen$DisplayCache;"))
    public void keyEnd(CallbackInfo ci) {
        updatePageMessage();
    }

    @Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;getDisplayCache()Lnet/minecraft/client/gui/screens/inventory/BookEditScreen$DisplayCache;"))
    public void mouseClicked(double d, double e, int i, CallbackInfoReturnable<Boolean> cir) {
        updatePageMessage();
    }

    @Inject(method = "mouseDragged", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;getDisplayCache()Lnet/minecraft/client/gui/screens/inventory/BookEditScreen$DisplayCache;"))
    public void mouseDragged(double d, double e, int i, double f, double g, CallbackInfoReturnable<Boolean> cir) {
        updatePageMessage();
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void addCustomButtons(CallbackInfo ci) {
        interpretButton = Button.builder(Component.literal("Run Code"), button -> {
            this.isModified = true;
            this.saveChanges(false);

            // TODO: fix this because this is a bit too much for a mixin :D
            StringBuilder builder = new StringBuilder();
            Preprocessor resolver = null;

            for (int i = 0; i < pages.size(); i++) {
                builder.append(new Preprocessor(pages.get(i)));
                builder.append(pages.get(i).replaceAll("![A-Za-z]+ [A-Za-z0-9]+", ""));

                if (i < pages.size() - 1) {
                    builder.append("\n".repeat(Math.max(0, 14 - pages.get(i).split("\n").length + 1)));
                }
            }
            CraftScript.run(builder.toString());

            this.currentPage = getNumPages() - 1;
            this.updateButtonVisibility();
            this.clearDisplayCache();

        }).bounds(this.width / 2 - 100, 196 + baseButtonHeight + 5, baseButtonWidth, baseButtonHeight).build();

        saveChangesButton = Button.builder(Component.literal("Save Changes"), button -> {
           this.saveChanges(false);
        }).bounds(this.width / 2 + 2, 196 + baseButtonHeight + 5, baseButtonWidth, baseButtonHeight).build();

        exportButton = Button.builder(Component.literal("Export code"), button -> {
            Exporter.export(book.getHoverName().getString(), pages);
        }).bounds(this.width / 2 - 100, 196 + 2 * (baseButtonHeight + 5), baseButtonWidth, baseButtonHeight).build();

        reloadModuleButton = Button.builder(Component.literal("Reload all modules"), button -> {
            Modules.reload();
        }).bounds(this.width / 2 + 2, 196 + 2 * (baseButtonHeight + 5), baseButtonWidth, baseButtonHeight).build();

        this.addRenderableWidget(interpretButton);
        this.addRenderableWidget(saveChangesButton);
        this.addRenderableWidget(exportButton);
        this.addRenderableWidget(reloadModuleButton);
    }

    @Redirect(method = "rebuildDisplayCache", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/StringSplitter;splitLines(Ljava/lang/String;ILnet/minecraft/network/chat/Style;ZLnet/minecraft/client/StringSplitter$LinePosConsumer;)V"))
    public void splitLines(StringSplitter instance, String string, int i, Style style, boolean bl, StringSplitter.LinePosConsumer linePosConsumer) {}

    @Inject(method = "rebuildDisplayCache", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/StringSplitter;splitLines(Ljava/lang/String;ILnet/minecraft/network/chat/Style;ZLnet/minecraft/client/StringSplitter$LinePosConsumer;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void splitLinesInjection(CallbackInfoReturnable<BookEditScreen.DisplayCache> cir, String string, int i, int j, IntList intList, List<BookEditScreen.LineInfo> list, MutableInt mutableInt, MutableBoolean mutableBoolean, StringSplitter stringSplitter) {
        stringSplitter.splitLines(string, 114, Style.EMPTY, true, (style, start, end) -> {
            int k = mutableInt.getAndIncrement();
            String string2 = string.substring(start, end);
            mutableBoolean.setValue(string2.endsWith("\n"));
            String string3 = StringUtils.stripEnd(string2, " \n");
            Objects.requireNonNull(this.font);
            int l = k * 9;
            BookEditScreen.Pos2i pos2i = this.convertLocalToScreen(new BookEditScreen.Pos2i(0, l));
            intList.add(start);
            list.add(new BookEditScreen.LineInfo(style, string3, pos2i.x, pos2i.y));
        });
    }


//    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I", ordinal = 3), index = 1)
//    public Component modifyComponentArg(Component component) {
//        MutableComponent newComponent = Component.empty();
//        String[] words = component.getString().split(" ");
//
//        Iterator<String> iterator = Arrays.stream(words).iterator();
//
//        while (iterator.hasNext()) {
//            String word = iterator.next();
//
//            int textColor = SYNTAX_HIGHLIGHTER.getTextColor(word);
//            Component wordComponent = Component.literal(word).withColor(textColor);
//
//            newComponent.append(wordComponent);
//
//            if (iterator.hasNext()) {
//                newComponent.append(CommonComponents.space());
//            }
//        }
//
//        return newComponent;
//    }
}
