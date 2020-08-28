package michalec.connor.FChat.BungeeCord;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.Bukkit;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
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
        dataHandler.copyTemplateIfMissing("config.yml", "plugins/FChat/config.yml");
        dataHandler.copyTemplateIfMissing("data.yml", "plugins/FChat/data.yml");
        dataHandler.addFile("config", "plugins/FChat/config.yml");
        dataHandler.addFile("data", "plugins/FChat/data.yml");
        dataHandler.loadFileYAML("data");
        dataHandler.loadFileYAML("config");

        getProxy().getPluginManager().registerListener(this, this);

        BungeeCord.getInstance().registerChannel("BungeeCord");
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
                    String begin = dataIn.readUTF();                    //The beginning tag of the chat sequence
                    String sequence_colorSuffix = dataIn.readUTF();     //Will be appended to each element in the sequence(before each word)
                    String sequence = dataIn.readUTF();                 //Contains a String formatted like this *§c*§b*§a Which will alterate every word the player has at the stars
                    String close = dataIn.readUTF();                    //The ending tag of the chat sequence
                }
                else if(command.equals("requestPlayerChatSequence")) {
                    //Send the global_playerChatSequence to the server that requested it
                    int serverPort = Integer.valueOf(dataIn.readUTF());
                    sendServerPlayerChatSequence(serverPort); 
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(ChatEvent event) {
        /*
         * This event will format a player's chat, 
         * given that they have a sequence registered in playerChatSequence.
         */

        if(event.getSender() instanceof ProxiedPlayer) {
            //Check if the player has their uuid in the list, if so format their chat message accordingly, then send it back to the bukkit side to force the player to send that message
            if(true) {
                //event.setCancelled(true); //cancel the original message
                sendBukkitPlayerMessage((ProxiedPlayer) event.getSender(), "Test");
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
         *we cant actually send a hasmap over a byte array so this is where it encodes/serializes the 
         *hasmap objectto bytes with help from: https://stackoverflow.com/questions/42812670/how-can-an-arraylist-of-objectsarraylists-be-converted-to-an-array-of-bytes, https://stackoverflow.com/questions/8887197/reliably-convert-any-object-to-string-and-then-back-again/8887244 (2nd answer)
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
        System.out.println(serializedObject);

                    
        
        out.writeUTF(serializedObject);
        targetServer.sendData("BungeeCord", out.toByteArray());


    }
}