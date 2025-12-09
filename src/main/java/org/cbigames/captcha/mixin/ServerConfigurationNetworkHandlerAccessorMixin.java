package org.cbigames.captcha.mixin;


import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public interface ServerConfigurationNetworkHandlerAccessorMixin {
    @Accessor
    ConfigurationTask getCurrentTask();

    @Invoker("finishCurrentTask")
    void onTaskFinishedI(ConfigurationTask.Type key);
}
