package michalec.connor.FChat.BungeeCord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;


import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class FChat extends Plugin implements Listener {
    DataHandler dataHandler = new DataHandler(this);

    //This HashMap contains the formatting for each player and the corresponding chat data, formatted like:
    //  [UUID,[begin, sequence_colorSuffix, sequence, close]]
    HashMap<UUID, ArrayList<String>> global_playerChatSequence = new HashMap<UUID, ArrayList<String>>();
    //This will be sent to any bukkit server that requests it with the requestPlayerChatSequence command
    
    @Override
    public void onEnable() {
        dataHandler.createDirectoryIfMissing("plugins/FChat");
        dataHandler.copyTemplateIfMissing("data.yml", "plugins/FChat/data.yml");
        dataHandler.addFile("data", "plugins/FChat/data.yml");
        dataHandler.loadFileYAML("data");

        getProxy().getPluginManager().registerListener(this, this);

        BungeeCord.getInstance().registerChannel("BungeeCord");


        //Load chat sequence from data.yml
        global_playerChatSequence = loadDataChatSequence();
        
    }

    private HashMap<UUID, ArrayList<String>> loadDataChatSequence() {
        HashMap<UUID, ArrayList<String>> out = new HashMap<UUID, ArrayList<String>>();

        Collection<String> players = dataHandler.getConfigurationSections("data", "PlayerData");
        for(String player : players) {
            if(dataHandler.getYAMLBooleanField("data", "PlayerData."+player+".empty") != true) {
                ArrayList<String> playerData = new ArrayList<String>();
                playerData.add(dataHandler.getYAMLStringField("data", "PlayerData."+player+".begin"));
                playerData.add(dataHandler.getYAMLStringField("data", "PlayerData."+player+".sequence_colorSuffix"));
                playerData.add(dataHandler.getYAMLStringField("data", "PlayerData."+player+".sequence"));
                playerData.add(dataHandler.getYAMLStringField("data", "PlayerData."+player+".close"));

                out.put(UUID.fromString(player), playerData);
            }
        }

        return(out);
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent event) {
        /*
         * This event will trigger when commands are sent through the bungeeCord channel.
         * When a message is received, check to see if it's coming from the FChat subchannel.
         * This handles when a player does /chatcolor(on bukkit end), and choses a color the information will be sent here(after clicking "apply")
         */

        if(event.getTag().equals("BungeeCord")) {   //Check to make sure it's coming from the BungeeCord channel
            ByteArrayDataInput dataIn = ByteStreams.newDataInput(event.getData());
            String subChannel = dataIn.readUTF();   //Get the subchannel, note that readUTF reads the next line in the array
            if(subChannel.equals("FChat")) {
                String command = dataIn.readUTF();  //The command, make sure it's handlePlayerColor
                if(command.equals("handlePlayerColor")) {
                    String player = dataIn.readUTF();                   //Player that is changing their color
                    String begin = dataIn.readUTF();                    //The beginning tag of the chat sequence
                    String sequence_colorSuffix = dataIn.readUTF();     //Will be appended to each element in the sequence(before each word)
                    String sequence = dataIn.readUTF();                 //Contains a String formatted like this *§c*§b*§a Which will alterate every word the player has at the stars
                    String close = dataIn.readUTF();                    //The ending tag of the chat sequence

                    ArrayList<String> newColor = new ArrayList<String>();
                    newColor.add(begin);
                    newColor.add(sequence_colorSuffix);
                    newColor.add(sequence);
                    newColor.add(close);

                    UUID playerUUID = this.getProxy().getPlayer(player).getUniqueId();

                    //Check if the arrayList is completely empty, if it is, remove that player from the arrayList(if they are currently in it), and set the empty flag in yaml to true so they wont be added to it on server start
                    int colorIndex;
                    for(colorIndex = 0; colorIndex<newColor.size(); colorIndex++) {
                        if(newColor.get(colorIndex).length()!=0)
                            break;
                    }
;
                    if(colorIndex != newColor.size()) {
                        //At least one is filled
                        global_playerChatSequence.put(
                            this.getProxy().getPlayer(player).getUniqueId(), newColor);

                        dataHandler.setYAMLField("data", "PlayerData."+playerUUID+".empty", false);
                        dataHandler.setYAMLField("data", "PlayerData."+playerUUID+".begin", begin);
                        dataHandler.setYAMLField("data", "PlayerData."+playerUUID+".sequence_colorSuffix", sequence_colorSuffix);
                        dataHandler.setYAMLField("data", "PlayerData."+playerUUID+".sequence", sequence);
                        dataHandler.setYAMLField("data", "PlayerData."+playerUUID+".close", close);
                    }
                    else {
                        //It is empty
                        dataHandler.setYAMLField("data", "PlayerData."+playerUUID+".empty", true);
                        //Copy the rest of the fields just for neatness, although it wont actually matter because of the empty field
                        dataHandler.setYAMLField("data", "PlayerData."+playerUUID+".begin", begin);
                        dataHandler.setYAMLField("data", "PlayerData."+playerUUID+".sequence_colorSuffix", sequence_colorSuffix);
                        dataHandler.setYAMLField("data", "PlayerData."+playerUUID+".sequence", sequence);
                        dataHandler.setYAMLField("data", "PlayerData."+playerUUID+".close", close);
                    }
                }
                else if(command.equals("requestPlayerChatSequence")) {
                    //Send the global_playerChatSequence to the server that requested it
                    int serverPort = Integer.valueOf(dataIn.readUTF());
                    sendServerPlayerChatSequence(serverPort); 
                }
            }
        }
    }

  


    public void sendBukkitPlayerMessage(ProxiedPlayer player, String message) {
        //send bukkit the data to force the player to send a message
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("FChat");                                //Subchannel
        out.writeUTF("forcePlayerSend");                      //command
        out.writeUTF(player.getName());                       //player
        out.writeUTF(message);                                //message

        player.getServer().sendData("BungeeCord", out.toByteArray()); //The first arg is what channel to send on, this can be changed

    }

    public void sendServerPlayerChatSequence(int serverTargetPort) {

        //Header info
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("FChat");
        out.writeUTF("updatedPlayerChatSequence");

        //Get the server to send the data to using the port
        ServerInfo targetServer = null;
        for(ServerInfo testServer : this.getProxy().getServers().values()) {
            int thisServerPort = ((InetSocketAddress) testServer.getSocketAddress()).getPort();
            if(serverTargetPort == thisServerPort) {
                //this is the server to send to
                targetServer = testServer;
            }
        }

        /*
         * we cant actually send a hasmap over a byte array so this is where it encodes/serializes the 
         * hashmap object to bytes with help from: https://stackoverflow.com/questions/42812670/how-can-an-arraylist-of-objectsarraylists-be-converted-to-an-array-of-bytes, https://stackoverflow.com/questions/8887197/reliably-convert-any-object-to-string-and-then-back-again/8887244 (2nd answer)
         * It is important to convert the string to base64 first to avoid the corruption that comes with utf-8
         */

        ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
        ObjectOutputStream outObjectStream = null;
        try {
            outObjectStream = new ObjectOutputStream(outByteStream);
            outObjectStream.writeObject(global_playerChatSequence);
            outObjectStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] encodedObject = Base64.getEncoder().encode(outByteStream.toByteArray());
        String serializedObject = new String(encodedObject); //Convert the Base64 bytes to UTF

                    
        
        out.writeUTF(serializedObject);
        targetServer.sendData("BungeeCord", out.toByteArray());


    }
}