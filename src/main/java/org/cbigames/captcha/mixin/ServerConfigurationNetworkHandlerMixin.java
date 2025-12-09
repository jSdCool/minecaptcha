package org.cbigames.captcha.mixin;

import org.cbigames.captcha.CaptchaTask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationNetworkHandlerMixin {

    @Final
    @Shadow
    private Queue<ConfigurationTask> configurationTasks;

    @Inject(method = "addOptionalTasks()V", at = @At("RETURN"))
    protected void queueSendResourcePackTask(CallbackInfo ci){
        configurationTasks.add(new CaptchaTask());
    }

}
