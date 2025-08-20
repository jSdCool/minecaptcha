package org.cbigames.captcha.mixin;


import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerConfigurationNetworkHandler.class)
public interface ServerConfigurationNetworkHandlerAccessorMixin {
    @Accessor
    ServerPlayerConfigurationTask getCurrentTask();

    @Invoker("onTaskFinished")
    void onTaskFinishedI(ServerPlayerConfigurationTask.Key key);
}
