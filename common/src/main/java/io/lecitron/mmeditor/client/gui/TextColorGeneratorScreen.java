package io.lecitron.mmeditor.client.gui;

import io.lecitron.mmeditor.client.MMEditorClient;
import io.lecitron.mmeditor.client.text.LegacyTextColorGenerator;
import io.lecitron.mmeditor.mixin.client.EditBoxAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class TextColorGeneratorScreen extends Screen {
    private static final int[] PALETTE_COLORS = {
            0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xFF55FF,
            0xFFFFFF, 0xAAAAAA, 0x555555, 0xAA0000, 0x00AA00, 0x0000AA, 0x111111
    };
    private static final int PALETTE_COLUMNS = 7;
    private static final int PALETTE_SIZE = 14;
    private static final int PALETTE_GAP = 4;

    private final Screen parent;
    private final EditBox targetBox;
    private final boolean initialGradientMode;
    private final String initialTargetValue;
    private final int selectionStart;
    private final int selectionEnd;
    private final boolean hasInitialSelection;

    private EditBox contentInput;
    private EditBox startColorInput;
    private EditBox endColorInput;
    private Button modeButton;
    private Button outputButton;
    private boolean gradientMode;
    private boolean miniMessageOutput;
    private Component feedback = CommonComponents.EMPTY;
    private int feedbackColor = 0xFF5555;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int paletteLeft;
    private int paletteTop;
    private int paletteRows;
    private int actionButtonY;

    public TextColorGeneratorScreen(Screen parent, EditBox targetBox, boolean gradientMode) {
        super(Component.literal("Text Color Generator"));
        this.parent = parent;
        this.targetBox = targetBox;
        this.initialGradientMode = gradientMode;
        this.initialTargetValue = targetBox.getValue();

        int cursorPos = targetBox.getCursorPosition();
        int highlightPos = ((EditBoxAccessor) targetBox).mmeditor$getHighlightPos();
        this.selectionStart = Math.min(cursorPos, highlightPos);
        this.selectionEnd = Math.max(cursorPos, highlightPos);
        this.hasInitialSelection = this.selectionEnd > this.selectionStart;
    }

    @Override
    protected void init() {
        MMEditorClient.Config config = MMEditorClient.config();
        this.gradientMode = this.initialGradientMode;
        this.miniMessageOutput = config.miniMessageOutput();
        this.panelWidth = 280;
        this.panelLeft = this.width / 2 - this.panelWidth / 2;
        this.panelTop = Math.max(12, this.height / 2 - 130);

        String plainText;
        if (this.hasInitialSelection && this.selectionEnd <= this.initialTargetValue.length()) {
            plainText = LegacyTextColorGenerator.stripLegacyColorCodes(this.initialTargetValue.substring(this.selectionStart, this.selectionEnd));
        } else {
            plainText = LegacyTextColorGenerator.stripLegacyColorCodes(this.initialTargetValue);
        }

        this.contentInput = new EditBox(this.font, this.panelLeft, this.panelTop + 28, this.panelWidth, 20, Component.literal("Text"));
        this.contentInput.setMaxLength(512);
        this.contentInput.setValue(plainText);
        this.addRenderableWidget(this.contentInput);

        this.startColorInput = new EditBox(this.font, this.panelLeft, this.panelTop + 62, this.panelWidth, 20, Component.literal("Start Color"));
        this.startColorInput.setMaxLength(7);
        this.startColorInput.setValue(LegacyTextColorGenerator.formatHexColor(config.suggestedStartColor()));
        this.addRenderableWidget(this.startColorInput);

        this.endColorInput = new EditBox(this.font, this.panelLeft, this.panelTop + 96, this.panelWidth, 20, Component.literal("End Color"));
        this.endColorInput.setMaxLength(7);
        this.endColorInput.setValue(LegacyTextColorGenerator.formatHexColor(config.suggestedEndColor()));
        this.addRenderableWidget(this.endColorInput);

        this.modeButton = this.addRenderableWidget(Button.builder(Component.empty(), button -> {
            this.gradientMode = !this.gradientMode;
            this.updateModeButton();
        }).bounds(this.panelLeft, this.panelTop + 128, 138, 20).build());

        this.outputButton = this.addRenderableWidget(Button.builder(Component.empty(), button -> {
            this.miniMessageOutput = !this.miniMessageOutput;
            this.updateOutputButton();
        }).bounds(this.panelLeft + 142, this.panelTop + 128, 138, 20).build());

        this.paletteRows = (PALETTE_COLORS.length + PALETTE_COLUMNS - 1) / PALETTE_COLUMNS;
        int paletteWidth = PALETTE_COLUMNS * PALETTE_SIZE + (PALETTE_COLUMNS - 1) * PALETTE_GAP;
        this.paletteLeft = this.panelLeft + (this.panelWidth - paletteWidth) / 2;
        this.paletteTop = this.panelTop + 168;

        this.actionButtonY = this.paletteTop + this.paletteRows * (PALETTE_SIZE + PALETTE_GAP) + 10;
        this.addRenderableWidget(Button.builder(Component.literal("Apply"), button -> this.applyAndClose())
                .bounds(this.panelLeft, this.actionButtonY, 138, 20)
                .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose())
                .bounds(this.panelLeft + 142, this.actionButtonY, 138, 20)
                .build());

        this.setInitialFocus(this.contentInput);
        this.updateModeButton();
        this.updateOutputButton();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.panelTop, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.literal("Input Text"), this.panelLeft, this.panelTop + 18, 0xA0A0A0, false);
        guiGraphics.drawString(this.font, Component.literal("Start Color (#RRGGBB)"), this.panelLeft, this.panelTop + 52, 0xA0A0A0, false);
        guiGraphics.drawString(this.font, Component.literal("End Color (#RRGGBB)"), this.panelLeft, this.panelTop + 86, this.gradientMode ? 0xA0A0A0 : 0x666666, false);
        guiGraphics.drawString(this.font, Component.literal("Palette (click swatch -> focused color input)"), this.panelLeft, this.panelTop + 156, 0xA0A0A0, false);
        guiGraphics.drawString(this.font, Component.literal("If text selected: only selected part is recolored"), this.panelLeft, this.actionButtonY + 24, 0x888888, false);

        this.renderPalette(guiGraphics, mouseX, mouseY);
        guiGraphics.drawCenteredString(this.font, this.feedback, this.width / 2, this.actionButtonY + 38, this.feedbackColor);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0) {
            int paletteIndex = this.paletteIndexAt((int) event.x(), (int) event.y());
            if (paletteIndex >= 0) {
                this.applyPaletteColor(PALETTE_COLORS[paletteIndex]);
                return true;
            }
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void applyAndClose() {
        try {
            int startColor = LegacyTextColorGenerator.parseHexColor(this.startColorInput.getValue());
            int endColor = this.gradientMode ? LegacyTextColorGenerator.parseHexColor(this.endColorInput.getValue()) : startColor;
            String text = this.contentInput.getValue();

            String generatedLegacy = this.gradientMode
                    ? LegacyTextColorGenerator.applyGradient(text, startColor, endColor)
                    : LegacyTextColorGenerator.applySolidColor(text, startColor);
            String legacyWithReset = generatedLegacy + "\u00A7r";
            String generatedAmpersand = LegacyTextColorGenerator.toAmpersandFormat(legacyWithReset);
            String generatedMiniMessage = this.gradientMode
                    ? "<gradient:" + LegacyTextColorGenerator.formatHexColor(startColor) + ":" + LegacyTextColorGenerator.formatHexColor(endColor) + ">" + text + "</gradient>"
                    : "<color:" + LegacyTextColorGenerator.formatHexColor(startColor) + ">" + text + "</color>";

            String[] segments = this.miniMessageOutput
                    ? new String[]{generatedMiniMessage, legacyWithReset, generatedAmpersand}
                    : new String[]{legacyWithReset, generatedAmpersand, generatedMiniMessage};

            for (String segment : segments) {
                String output = this.wrapOnSelection(segment);
                if (this.tryApplyToTarget(output)) {
                    this.copyToClipboard(output);
                    MMEditorClient.Config config = MMEditorClient.config();
                    config.rememberColors(startColor, endColor);
                    config.setMiniMessageOutput(this.miniMessageOutput);
                    this.onClose();
                    return;
                }
            }

            this.feedback = Component.literal("Current textbox rejected generated text");
            this.feedbackColor = 0xFF5555;
        } catch (IllegalArgumentException exception) {
            this.feedback = Component.literal("Invalid color, use #RRGGBB");
            this.feedbackColor = 0xFF5555;
        }
    }

    private void applyPaletteColor(int color) {
        String hex = LegacyTextColorGenerator.formatHexColor(color);
        if (this.gradientMode && this.endColorInput.isFocused()) {
            this.endColorInput.setValue(hex);
            this.feedback = Component.literal("Palette -> End Color " + hex);
        } else {
            this.startColorInput.setValue(hex);
            if (!this.gradientMode) {
                this.endColorInput.setValue(hex);
            }
            this.feedback = Component.literal("Palette -> Start Color " + hex);
        }
        this.feedbackColor = 0x55FF55;
    }

    private void renderPalette(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int hoveredIndex = this.paletteIndexAt(mouseX, mouseY);
        int startColor = this.parseColorSafe(this.startColorInput.getValue(), -1);
        int endColor = this.parseColorSafe(this.endColorInput.getValue(), -1);

        for (int index = 0; index < PALETTE_COLORS.length; index++) {
            int row = index / PALETTE_COLUMNS;
            int column = index % PALETTE_COLUMNS;
            int x = this.paletteLeft + column * (PALETTE_SIZE + PALETTE_GAP);
            int y = this.paletteTop + row * (PALETTE_SIZE + PALETTE_GAP);
            int color = PALETTE_COLORS[index];

            int border = 0xFF202020;
            if (index == hoveredIndex) {
                border = 0xFFFFFFAA;
            } else if (color == startColor) {
                border = 0xFF66FF66;
            } else if (this.gradientMode && color == endColor) {
                border = 0xFF66AAFF;
            }

            guiGraphics.fill(x - 1, y - 1, x + PALETTE_SIZE + 1, y + PALETTE_SIZE + 1, border);
            guiGraphics.fill(x, y, x + PALETTE_SIZE, y + PALETTE_SIZE, 0xFF000000 | color);
        }

        if (hoveredIndex >= 0) {
            String hex = LegacyTextColorGenerator.formatHexColor(PALETTE_COLORS[hoveredIndex]);
            this.drawHint(guiGraphics, mouseX, mouseY, Component.literal("Palette " + hex));
        }
    }

    private int paletteIndexAt(int mouseX, int mouseY) {
        for (int index = 0; index < PALETTE_COLORS.length; index++) {
            int row = index / PALETTE_COLUMNS;
            int column = index % PALETTE_COLUMNS;
            int x = this.paletteLeft + column * (PALETTE_SIZE + PALETTE_GAP);
            int y = this.paletteTop + row * (PALETTE_SIZE + PALETTE_GAP);
            if (mouseX >= x && mouseX < x + PALETTE_SIZE && mouseY >= y && mouseY < y + PALETTE_SIZE) {
                return index;
            }
        }
        return -1;
    }

    private void drawHint(GuiGraphics guiGraphics, int mouseX, int mouseY, Component text) {
        int width = this.font.width(text) + 8;
        int height = 12;
        int x = mouseX + 8;
        int y = mouseY - 14;
        if (x + width > this.width - 2) {
            x = this.width - width - 2;
        }
        if (y < 2) {
            y = mouseY + 10;
        }
        if (y + height > this.height - 2) {
            y = this.height - height - 2;
        }

        guiGraphics.fill(x, y, x + width, y + height, 0xE0101010);
        guiGraphics.drawString(this.font, text, x + 4, y + 2, 0xFFFFFF, false);
    }

    private int parseColorSafe(String text, int fallback) {
        try {
            return LegacyTextColorGenerator.parseHexColor(text);
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    private boolean tryApplyToTarget(String value) {
        String before = this.targetBox.getValue();
        this.targetBox.setValue(value);
        String after = this.targetBox.getValue();
        return after.equals(value) || !after.equals(before);
    }

    private String wrapOnSelection(String generatedSegment) {
        if (!this.hasInitialSelection || this.selectionEnd > this.initialTargetValue.length()) {
            return generatedSegment;
        }
        return this.initialTargetValue.substring(0, this.selectionStart)
                + generatedSegment
                + this.initialTargetValue.substring(this.selectionEnd);
    }

    private void copyToClipboard(String value) {
        if (this.minecraft != null) {
            this.minecraft.keyboardHandler.setClipboard(value);
        }
    }

    private void updateModeButton() {
        this.modeButton.setMessage(this.gradientMode ? Component.literal("Mode: Gradient") : Component.literal("Mode: Solid"));
        this.endColorInput.active = this.gradientMode;
    }

    private void updateOutputButton() {
        this.outputButton.setMessage(this.miniMessageOutput ? Component.literal("Output: MiniMessage") : Component.literal("Output: Legacy"));
    }
}
