package org.cbigames.captcha;

import net.minecraft.dialog.AfterAction;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.action.DynamicCustomDialogAction;
import net.minecraft.dialog.body.DialogBody;
import net.minecraft.dialog.body.PlainMessageDialogBody;
import net.minecraft.dialog.input.TextInputControl;
import net.minecraft.dialog.type.DialogInput;
import net.minecraft.dialog.type.NoticeDialog;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.ShowDialogS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CaptchaTask implements ServerPlayerConfigurationTask {

    public static final Key KEY = new Key("captcha");
    public static final Identifier CLICK_EVENT_ID = Identifier.of(Minecaptcha.MOD_ID,"captcha_response");

    private final String captchaValue;

    public CaptchaTask(){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<13;i++){
            String LETTERS = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKMLNOPQRSTUVWXYZ";
            sb.append(LETTERS.charAt((int)(Math.random()* LETTERS.length())));
        }
        captchaValue = sb.toString();
    }

    @Override
    public Key getKey() {
        return KEY;
    }
    @Override
    public void sendPacket(Consumer<Packet<?>> sender) {

        MutableText captchaText = Text.literal(captchaValue);
        captchaText.setStyle(captchaText.getStyle().withBold(true).withColor(TextColor.fromRgb(0x00FFFF)));

        List<DialogBody> body = new ArrayList<>();
        body.add(new PlainMessageDialogBody(Text.of("Enter the text below to prove your not a robot"),300));
        body.add(new PlainMessageDialogBody(captchaText,300));

        List<DialogInput> inputs = new ArrayList<>();
        inputs.add(new DialogInput("response",new TextInputControl(300,Text.of("Response"),true,"",40,Optional.empty())));

        NbtCompound responseCompound = new NbtCompound();
        responseCompound.putString("response","");

        sender.accept( new ShowDialogS2CPacket(RegistryEntry.of(
                new NoticeDialog(
                    new DialogCommonData(Text.of("Captcha"), Optional.empty(),false,true, AfterAction.CLOSE,body,inputs),
                    new DialogActionButtonData(
                        new DialogButtonData(Text.of("I am not a Robot"),300),
                        Optional.of(new DynamicCustomDialogAction(CLICK_EVENT_ID, Optional.of(responseCompound)))
                    )
                )
        )));
    }

    public boolean handleResponse(NbtCompound response){
        String captchaResponse = response.getString("response","");

        return captchaResponse.equals(captchaValue);
    }
}
