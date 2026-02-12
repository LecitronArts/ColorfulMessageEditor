package io.lecitron.mmeditor.neoforge;

import io.lecitron.mmeditor.MMEditor;
import net.neoforged.fml.common.Mod;

@Mod(MMEditor.MOD_ID)
public final class MMEditorNeoForge {
    public MMEditorNeoForge() {
        // Run our common setup.
        MMEditor.init();
    }
}
