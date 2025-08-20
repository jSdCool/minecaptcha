package org.cbigames.captcha.mixin;

import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import org.cbigames.captcha.CaptchaTask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class ServerConfigurationNetworkHandlerMixin {

    @Final
    @Shadow
    private Queue<ServerPlayerConfigurationTask> tasks;

    @Inject(method = "queueSendResourcePackTask()V", at = @At("RETURN"))
    protected void queueSendResourcePackTask(CallbackInfo ci){
        tasks.add(new CaptchaTask());
    }

}
