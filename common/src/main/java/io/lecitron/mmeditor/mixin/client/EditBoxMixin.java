package io.lecitron.mmeditor.mixin.client;

import io.lecitron.mmeditor.client.MMEditorClient;
import io.lecitron.mmeditor.client.gui.TextColorGeneratorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public abstract class EditBoxMixin {
    @Inject(method = "renderWidget", at = @At("TAIL"))
    private void mmeditor$renderColorButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!MMEditorClient.config().showEditBoxButtons()) {
            return;
        }

        if (Minecraft.getInstance().screen instanceof TextColorGeneratorScreen) {
            return;
        }

        EditBox self = (EditBox) (Object) this;
        int iconSize = mmeditor$iconSize(self);
        if (iconSize < 8) {
            return;
        }

        int gradientX = mmeditor$gradientIconX(self, iconSize);
        int solidX = mmeditor$solidIconX(self, iconSize);
        int iconY = mmeditor$iconY(self, iconSize);

        boolean gradientHovered = mmeditor$contains(mouseX, mouseY, gradientX, iconY, iconSize);
        boolean solidHovered = mmeditor$contains(mouseX, mouseY, solidX, iconY, iconSize);

        mmeditor$drawIcon(guiGraphics, gradientX, iconY, iconSize, gradientHovered, "G");
        mmeditor$drawIcon(guiGraphics, solidX, iconY, iconSize, solidHovered, "S");

        if (gradientHovered) {
            mmeditor$drawHoverHint(guiGraphics, mouseX, mouseY, Component.literal("Generate gradient text"));
        } else if (solidHovered) {
            mmeditor$drawHoverHint(guiGraphics, mouseX, mouseY, Component.literal("Generate solid color text"));
        }
    }

    @Inject(method = "onClick", at = @At("HEAD"), cancellable = true)
    private void mmeditor$handleColorButtonClick(MouseButtonEvent event, boolean isDoubleClick, CallbackInfo ci) {
        if (!MMEditorClient.config().showEditBoxButtons()) {
            return;
        }

        if (Minecraft.getInstance().screen instanceof TextColorGeneratorScreen) {
            return;
        }

        if (event.button() != 0) {
            return;
        }

        EditBox self = (EditBox) (Object) this;
        int iconSize = mmeditor$iconSize(self);
        if (iconSize < 8) {
            return;
        }

        int gradientX = mmeditor$gradientIconX(self, iconSize);
        int solidX = mmeditor$solidIconX(self, iconSize);
        int iconY = mmeditor$iconY(self, iconSize);

        boolean gradientClicked = mmeditor$contains(event.x(), event.y(), gradientX, iconY, iconSize);
        boolean solidClicked = mmeditor$contains(event.x(), event.y(), solidX, iconY, iconSize);
        if (!gradientClicked && !solidClicked) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Screen currentScreen = minecraft.screen;
        if (currentScreen == null) {
            return;
        }

        minecraft.setScreen(new TextColorGeneratorScreen(currentScreen, self, gradientClicked));
        ci.cancel();
    }

    private static int mmeditor$iconSize(EditBox box) {
        int size = Math.min(12, box.getHeight() - 4);
        if (box.getWidth() < 54) {
            return 0;
        }
        return Math.max(size, 8);
    }

    private static int mmeditor$gradientIconX(EditBox box, int iconSize) {
        int padding = 2;
        int gap = 2;
        return box.getX() + box.getWidth() - padding - iconSize * 2 - gap;
    }

    private static int mmeditor$solidIconX(EditBox box, int iconSize) {
        int padding = 2;
        return box.getX() + box.getWidth() - padding - iconSize;
    }

    private static int mmeditor$iconY(EditBox box, int iconSize) {
        return box.getY() + (box.getHeight() - iconSize) / 2;
    }

    private static boolean mmeditor$contains(double mouseX, double mouseY, int x, int y, int size) {
        return mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
    }

    private static void mmeditor$drawIcon(GuiGraphics guiGraphics, int x, int y, int size, boolean hovered, String text) {
        int fillColor = hovered ? 0xDD3A3A3A : 0xCC202020;
        guiGraphics.fill(x, y, x + size, y + size, fillColor);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, x + size / 2, y + (size - 8) / 2, 0xFFFFFF);
    }

    private static void mmeditor$drawHoverHint(GuiGraphics guiGraphics, int mouseX, int mouseY, Component text) {
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.font.width(text) + 8;
        int height = 12;
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int x = mouseX + 8;
        int y = mouseY - 14;
        if (x + width > screenWidth - 2) {
            x = screenWidth - width - 2;
        }
        if (y < 2) {
            y = mouseY + 10;
        }
        if (y + height > screenHeight - 2) {
            y = screenHeight - height - 2;
        }

        guiGraphics.fill(x, y, x + width, y + height, 0xE0101010);
        guiGraphics.drawString(minecraft.font, text, x + 4, y + 2, 0xFFFFFF, false);
    }
}
