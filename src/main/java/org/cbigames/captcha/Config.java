package org.cbigames.captcha;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Config {

    private Config(){
        try (Scanner fileIn = new Scanner(new File("config/minecaptcha.cfg"))){
            while (fileIn.hasNextLine()){
                String line = fileIn.nextLine();

                if(line.startsWith("method=")){
                    String methodString = line.substring("method=".length());
                    method = CaptchaMethod.of(methodString);
                }
            }
        } catch (FileNotFoundException e) {
            saveNewConfig();
        }
    }

    private CaptchaMethod method = CaptchaMethod.TEXT;

    private static void saveNewConfig(){
        new File("config/").mkdirs();
        try (FileWriter fw = new FileWriter("config/minecaptcha.cfg")){
            fw.write("""
                    # captcha method, how the captcha will be conducted. options are: TEXT and IMAGE. Text mode will simply display a random text string for the user to enter. Image mode will display several images of text that the user will have to input. note, they will have to apply the resource pack for this to work. Image definitions are sourced from captchaimage.json
                    method=TEXT
                    """);
            fw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Config instance;

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
}


