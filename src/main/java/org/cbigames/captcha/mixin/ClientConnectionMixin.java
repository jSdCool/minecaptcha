package org.cbigames.captcha.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomClickActionC2SPacket;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.cbigames.captcha.CaptchaTask;
import org.cbigames.captcha.Minecaptcha;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {


    @Shadow
    private PacketListener packetListener;

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V"))
    private void handlePacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci){
        if(packet instanceof CustomClickActionC2SPacket(
                Identifier id, java.util.Optional<net.minecraft.nbt.NbtElement> payload
        )){
            if(id.equals(CaptchaTask.CLICK_EVENT_ID)){
                if(packetListener instanceof ServerConfigurationNetworkHandler networkHandler) {
                    //its our event

                    ClientConnection self = (ClientConnection) (Object) this;

                    ServerPlayerConfigurationTask task = ((ServerConfigurationNetworkHandlerAccessorMixin)networkHandler).getCurrentTask();

                    if(task instanceof CaptchaTask captchaTask) {
                        if(captchaTask.handleResponse(payload.orElse(new NbtCompound()).asCompound().orElse(new NbtCompound()))) {


                            ((ServerConfigurationNetworkHandlerAccessorMixin)networkHandler).onTaskFinishedI(CaptchaTask.KEY);
                        } else{
                            self.disconnect(Text.of("You are a Robot"));
                        }
                    } else {
                        Minecaptcha.LOGGER.error("Attempted to process captcha response but the current task was not a captcha!");
                        self.disconnect(Text.of("Internal Server Error!"));
                    }
                }else{
                    Minecaptcha.LOGGER.error("received captcha event when player was not in configuration phase");
                }
            }
        }
    }
}
