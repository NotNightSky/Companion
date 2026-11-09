package org.notnightsky.companion.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.notnightsky.companion.render.renderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void companion$onRender(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Minecraft.getInstance().gui.screen() == null) {
            renderer.render(graphics, 0, 0, deltaTracker.getRealtimeDeltaTicks());
        }
    }
}
