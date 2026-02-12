package io.lecitron.mmeditor.mixin.client;

import io.lecitron.mmeditor.client.gui.InlinePaletteClickHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    default void mmeditor$forwardInlinePaletteClick(MouseButtonEvent event, boolean isDoubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() != 0) {
            return;
        }

        ContainerEventHandler self = (ContainerEventHandler) this;
        GuiEventListener focused = self.getFocused();
        if (focused instanceof InlinePaletteClickHandler handler
                && handler.mmeditor$handleInlinePaletteClick(event.x(), event.y())) {
            cir.setReturnValue(true);
        }
    }
}
