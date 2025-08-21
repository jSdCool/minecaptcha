package org.cbigames.captcha;

import org.json.JSONObject;
import org.w3c.dom.ranges.RangeException;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Config {

    private Config(){
        try (Scanner fileIn = new Scanner(new File("config/minecaptcha.cfg"))){
            while (fileIn.hasNextLine()){
                String line = fileIn.nextLine();

                if(line.startsWith("method=")){
                    String methodString = line.substring("method=".length());
                    method = CaptchaMethod.of(methodString);
                }else if(line.startsWith("length=")){
                    String lengthStr = line.substring("length=".length());
                    captchaLength = Integer.parseInt(lengthStr);
                    if(captchaLength < 1 || captchaLength > 100){
                        throw new IndexOutOfBoundsException("Captcha length out of bounds [1,99]: "+captchaLength);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            saveNewConfig();
        }

        if(method == CaptchaMethod.IMAGE){
            try {
                FileInputStream fis = new FileInputStream("config/captchaimage.json");
                JSONObject valuesJson = Main.loadJSONObject(fis);
                fis.close();
                int numberImages = valuesJson.getInt("total images");
                for(int i=0;i<numberImages;i++){
                    valueMaps.add(new ImageValueMap(i,valuesJson.getInt(""+i)));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private CaptchaMethod method = CaptchaMethod.TEXT;
    private int captchaLength = 8;

    private final ArrayList<ImageValueMap> valueMaps = new ArrayList<>();

    private static Config instance;

    public int getNumberValues(){
        return valueMaps.size();
    }

    public int getValue(int index){
        return valueMaps.get(index).value();
    }

    public int getCaptchaLength() {
        return captchaLength;
    }

    private static void saveNewConfig(){
        new File("config/").mkdirs();
        try (FileWriter fw = new FileWriter("config/minecaptcha.cfg")){
            fw.write("""
                    # captcha method, how the captcha will be conducted. options are: TEXT and IMAGE. Text mode will simply display a random text string for the user to enter. Image mode will display several images of text that the user will have to input. note, they will have to apply the resource pack for this to work. Image definitions are sourced from captchaimage.json
                    method=TEXT
                    # captcha length, how long the supplied text is
                    length=8
                    """);
            fw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Config getConfig(){
        if(instance == null){
            instance = new Config();
        }
        return instance;
    }

    public enum CaptchaMethod{
        TEXT,
        IMAGE;

        public static CaptchaMethod of(String in){
            return switch (in.toLowerCase()){
                case "text" -> TEXT;
                case "image" -> IMAGE;
                default -> TEXT;
            };
        }
    }

    public CaptchaMethod getMethod() {
        return method;
    }

    public record ImageValueMap(int imageNumber, int value){}
}


