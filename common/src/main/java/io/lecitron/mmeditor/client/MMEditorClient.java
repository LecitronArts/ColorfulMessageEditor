package io.lecitron.mmeditor.client;

public final class MMEditorClient {
    private static final Config CONFIG = new Config();
    private static boolean initialized;

    private MMEditorClient() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        CONFIG.resetToDefaults();
        initialized = true;
    }

    public static Config config() {
        if (!initialized) {
            init();
        }
        return CONFIG;
    }

    private static int sanitizeRgb(int color) {
        return color & 0xFFFFFF;
    }

    public static final class Config {
        private boolean showEditBoxButtons;
        private boolean miniMessageOutput;
        private int defaultStartColor;
        private int defaultEndColor;
        private int recentStartColor;
        private int recentEndColor;

        private Config() {
        }

        private void resetToDefaults() {
            this.showEditBoxButtons = true;
            this.miniMessageOutput = false;
            this.defaultStartColor = 0xFFAA00;
            this.defaultEndColor = 0x55FFFF;
            this.recentStartColor = this.defaultStartColor;
            this.recentEndColor = this.defaultEndColor;
        }

        public boolean showEditBoxButtons() {
            return this.showEditBoxButtons;
        }

        public void setShowEditBoxButtons(boolean showEditBoxButtons) {
            this.showEditBoxButtons = showEditBoxButtons;
        }

        public boolean miniMessageOutput() {
            return this.miniMessageOutput;
        }

        public void setMiniMessageOutput(boolean miniMessageOutput) {
            this.miniMessageOutput = miniMessageOutput;
        }

        public int defaultStartColor() {
            return this.defaultStartColor;
        }

        public int defaultEndColor() {
            return this.defaultEndColor;
        }

        public void setDefaultColors(int defaultStartColor, int defaultEndColor) {
            this.defaultStartColor = sanitizeRgb(defaultStartColor);
            this.defaultEndColor = sanitizeRgb(defaultEndColor);
        }

        public int suggestedStartColor() {
            return this.recentStartColor;
        }

        public int suggestedEndColor() {
            return this.recentEndColor;
        }

        public void rememberColors(int startColor, int endColor) {
            this.recentStartColor = sanitizeRgb(startColor);
            this.recentEndColor = sanitizeRgb(endColor);
        }
    }
}
