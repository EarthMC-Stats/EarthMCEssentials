package net.emc.emce.mixins;

import io.github.emcw.entities.Player;
import net.emc.emce.utils.EarthMCAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.emc.emce.EarthMCEssentials.instance;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At("TAIL"), method="<init>")
    private void onInit(RunArgs args, CallbackInfo ci) {
        String clientName = args.network.session.getUsername();
        Player clientPlayer = EarthMCAPI.getPlayer(clientName);

        if (clientPlayer == null) System.out.println("Could not find player by client name: " + clientName);
        else {
            instance().setClientPlayer(clientPlayer);
            System.out.println(clientPlayer.asString());
        }
    }
}
