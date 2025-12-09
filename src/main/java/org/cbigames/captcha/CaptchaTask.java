package org.cbigames.captcha;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.NoticeDialog;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.dialog.input.TextInput;
import net.minecraft.server.network.ConfigurationTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CaptchaTask implements ConfigurationTask {

    public static final Type KEY = new Type("captcha");
    public static final ResourceLocation CLICK_EVENT_ID = ResourceLocation.fromNamespaceAndPath(Minecaptcha.MOD_ID,"captcha_response");

    private final String captchaValue;

    private final List<Integer> imageNumbers = new ArrayList<>();

    public CaptchaTask(){
        switch (Config.getConfig().getMethod()) {
            case TEXT -> {
                StringBuilder sb = new StringBuilder();
                int length = Config.getConfig().getCaptchaLength();
                for (int i = 0; i < length; i++) {
                    String LETTERS = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKMLNOPQRSTUVWXYZ";
                    sb.append(LETTERS.charAt((int) (Math.random() * LETTERS.length())));
                }
                captchaValue = sb.toString();
            }

            case IMAGE -> {
                int numberImages = Config.getConfig().getNumberValues();
                int length = Config.getConfig().getCaptchaLength();
                for(int i=0;i<length;i++) {
                    imageNumbers.add((int) (Math.random() * numberImages));
                }
                StringBuilder sb = new StringBuilder();
                for(int num: imageNumbers){
                    sb.append(Config.getConfig().getValue(num));
                }

                captchaValue = sb.toString();
                //Minecaptcha.LOGGER.info("Sending captcha: "+captchaValue);
            }
            case null, default -> captchaValue = "ERROR";
        }
    }

    @Override
    public @NotNull Type type() {
        return KEY;
    }
    @Override
    public void start(Consumer<Packet<?>> sender) {

        MutableComponent captchaText = Component.literal(captchaValue);
        captchaText.setStyle(captchaText.getStyle().withBold(true).withColor(TextColor.fromRgb(0x00FFFF)));

        List<DialogBody> body = new ArrayList<>();
        body.add(new PlainMessage(Component.nullToEmpty("Enter the text below to prove your not a robot"),300));

        switch (Config.getConfig().getMethod()) {
            case TEXT -> body.add(new PlainMessage(captchaText, 300));
            case IMAGE -> {
                MutableComponent images = Component.literal(".");
                images.setStyle(images.getStyle().withBold(true));
                for(int i:imageNumbers){
                      images.append(Component.object(new AtlasSprite(AtlasSprite.DEFAULT_ATLAS,ResourceLocation.fromNamespaceAndPath("captcha","item/img"+i))));

                }
                body.add(new PlainMessage(images,300));
                body.add(new PlainMessage(Component.nullToEmpty("note: sometimes these numbers may not be accurate"),300));
            }
        }

        List<Input> inputs = new ArrayList<>();
        inputs.add(new Input("response",new TextInput(300,Component.nullToEmpty("Response"),true,"",40,Optional.empty())));

        CompoundTag responseCompound = new CompoundTag();
        responseCompound.putString("response","");

        sender.accept( new ClientboundShowDialogPacket(Holder.direct(
                new NoticeDialog(
                    new CommonDialogData(Component.nullToEmpty("Captcha"), Optional.empty(),false,true, DialogAction.CLOSE,body,inputs),
                    new ActionButton(
                        new CommonButtonData(Component.nullToEmpty("I am not a Robot"),300),
                        Optional.of(new CustomAll(CLICK_EVENT_ID, Optional.of(responseCompound)))
                    )
                )
        )));
    }

    public boolean handleResponse(CompoundTag response){
        String captchaResponse = response.getStringOr("response","");
        //Minecaptcha.LOGGER.info("Received response: "+captchaResponse+" compaired to: "+captchaValue);
        return captchaResponse.equals(captchaValue);
    }
}
