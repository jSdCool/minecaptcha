# MineCaptcha
___
Vanilla compatible captcha test for minecraft java servers designed to help prevent automated bots from joining your server

> [!NOTE]  
> This is **NOT** a bulletproof solution for preventing bots from joining your server

## INFO
This mod currently offers 2 different types of captchas that server owners can choose from  
**TEXT**:  
This mode has the player enter a simple string of displayed minecraft text  
**IMAGE**:  
This mode has the player enter numbers from a series of images presented to them, while this mode is more secure it also requires a bit more setup, see below for instructions


## Image mode setup
1. Run the server with this mod to generate the config file
2. Edit the config file for this mod `minecaptcha.cfg` setting `method` to `IMAGE`
3. Execute the mod as a java program to generate the resource pack and number image mappings
```sh
java -jar minecaptcha-x.x.x.jar
```
4. Setup `resourcepack.zip` to be distributed as or combined with your server resource pack
5. Copy `captchaimage.json` into the config folder for your server
It is a good idea to re generate this resource pack and json file with some regularity  
note: when the resource pack is change it is REQUIRED to also change `captchaimage.json`. 