package org.cbigames.captcha;

//import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        //load the labels json
        JSONObject rawLabels = loadJSONObject(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("captcha/labels.json")));
        int numberOfImages = rawLabels.getInt("total images");
        ArrayList<valueMap> valueMaps = new ArrayList<>();
        //load the value mappings into a rearrangable formatt
        for(int i=0;i<numberOfImages;i++){
            valueMaps.add(new valueMap(i,rawLabels.getInt(""+i)));
        }
        //randomize the order
        ArrayList<valueMap> newMap = new ArrayList<>();
        while(!valueMaps.isEmpty()){
            newMap.add(valueMaps.remove((int)(Math.random()*valueMaps.size())));
        }
        //save the new mappings to a new json object
        JSONObject newLabels = new JSONObject();
        for(int i=0;i<newMap.size();i++){
            newLabels.put(""+i,newMap.get(i).imageValue());
        }
        newLabels.put("total images",newMap.size());
        //save the new json file
        Writer labelSaver = new PrintWriter(new FileOutputStream("captchaimage.json"));
        newLabels.write(labelSaver);
        labelSaver.flush();
        labelSaver.close();
        System.out.println("Generated captchaimage.json");

        //start on the resource pack

        ZipOutputStream resourcePackOut = new ZipOutputStream(new FileOutputStream("resourcepack.zip"));
        PrintWriter zipTextWriter = new PrintWriter(resourcePackOut);

        //add the Mcmeta file to the resource pack
        generateMcmeta(resourcePackOut,zipTextWriter);

        //copy the images into the resource pack
        copyImagesToResourcePack(resourcePackOut,newMap);

        resourcePackOut.flush();
        resourcePackOut.close();

        System.out.println("generated resourcepack.zip");

        System.out.println();

        System.out.println("Distribute resourcepack.zip to the players on your server");

        System.out.println("Place captchaimage.json inside the config folder for your server");
        System.out.println("NEVER SHARE captchaimage.json WITH ANYONE!");
    }

    //assets/captcha/
      //textures/item/<image here> CHECK
      //models/item/<item model defs here>

    //assets/minecraft/
      //items/command_block_minecart.json

    private static void copyImagesToResourcePack(ZipOutputStream output, List<valueMap> imageMappings) throws IOException {
        for(int i=0;i<imageMappings.size();i++){
            copyImageIntoResourcePack(output,imageMappings.get(i),i);
        }
    }

    private static void copyImageIntoResourcePack(ZipOutputStream output,valueMap map,int imageNumber) throws IOException {
        output.putNextEntry(new ZipEntry("assets/captcha/textures/item/img"+imageNumber+".png"));
        InputStream imageIn = Main.class.getClassLoader().getResourceAsStream("captcha/images/"+map.srcImageNumber+".png");
        assert imageIn != null;
        imageIn.transferTo(output);
        imageIn.close();
        output.closeEntry();
    }

    private static void generateMcmeta(ZipOutputStream output, PrintWriter textOut) throws IOException {
        output.putNextEntry(new ZipEntry("pack.mcmeta"));
        textOut.println("{");
            textOut.println("  \"pack\": {");
                textOut.println("    \"pack_format\": 69.0,");
                textOut.println("    \"description\": \"The number textures for the captcha\"");
            textOut.println("  }");
        textOut.println("}");
        textOut.flush();
        output.closeEntry();
    }


    public static JSONObject loadJSONObject(InputStream in) throws IOException {
        StringBuilder rawContent = new StringBuilder();
        int bytesRead;
        byte[] buffer = new byte[1024];
        while((bytesRead = in.read(buffer)) != -1) {
            rawContent.append(new String(buffer, 0, bytesRead));
        }

        return new JSONObject(rawContent.toString());

    }

    record valueMap(int srcImageNumber,int imageValue){}
}
