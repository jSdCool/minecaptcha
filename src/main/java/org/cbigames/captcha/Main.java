package org.cbigames.captcha;

import org.json.JSONArray;
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

        //generate the item models for this and add them to the resource pack
        generateAllItemModelDeffs(resourcePackOut,zipTextWriter,numberOfImages);

        //generate the item texture overrides
        generateItemModelOverrides(resourcePackOut,zipTextWriter,numberOfImages);

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

    private static void generateItemModelOverrides(ZipOutputStream output,PrintWriter textOut,int numberOfImages) throws IOException {
        output.putNextEntry(new ZipEntry("assets/minecraft/items/command_block_minecart.json"));

        JSONObject itemJson = new JSONObject();
        JSONObject modelJson = new JSONObject();
        modelJson.put("type","minecraft:range_dispatch");
        modelJson.put("property","custom_model_data");
        JSONObject fallbackObject = new JSONObject();
        fallbackObject.put("type", "model");
        fallbackObject.put("model","item/command_block_minecart");
        modelJson.put("fallback",fallbackObject);
        JSONArray entriesArr = new JSONArray();
        for(int i=0;i<numberOfImages;i++){
            JSONObject entry = new JSONObject();
            entry.put("threshold",(float)i);
            JSONObject internalModel = new JSONObject();
            internalModel.put("type","model");
            internalModel.put("model","captcha:item/img"+i);
            entry.put("model",internalModel);
            entriesArr.put(entry);
        }
        modelJson.put("entries",entriesArr);
        itemJson.put("model",modelJson);

        itemJson.write(textOut);
        textOut.flush();
        output.closeEntry();
    }

    private static void generateAllItemModelDeffs(ZipOutputStream output,PrintWriter textOut,int numberOfImages) throws IOException {
        for(int i=0;i<numberOfImages;i++){
            generateItemModelDeff(output,textOut,i);
        }
    }

    private static void generateItemModelDeff(ZipOutputStream output, PrintWriter textOut,int imageNumber) throws IOException {
        JSONObject modelJSON = new JSONObject();
        JSONArray sizeArr = new JSONArray();
        sizeArr.put(28);
        sizeArr.put(28);
        modelJSON.put("texture_size",sizeArr);

        JSONObject textureJson = new JSONObject();
        textureJson.put("layer0","captcha:item/img"+imageNumber);
        textureJson.put("particle","captcha:item/img"+imageNumber);
        modelJSON.put("textures",textureJson);
        modelJSON.put("parent","item/generated");

        output.putNextEntry(new ZipEntry("assets/captcha/models/item/img"+imageNumber+".json"));
        modelJSON.write(textOut);
        textOut.flush();
        output.closeEntry();
    }

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
                textOut.println("    \"pack_format\": 64,");
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
