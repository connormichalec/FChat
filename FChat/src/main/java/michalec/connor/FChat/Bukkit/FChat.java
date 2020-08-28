package michalec.connor.FChat.Bukkit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;


public class FChat extends JavaPlugin implements PluginMessageListener, Listener {

    // Local cache of the playerChatSequence
    HashMap<UUID, ArrayList<String>> playerChatSequence = new HashMap<UUID, ArrayList<String>>();

    //Avoid triggering chat event
    ArrayList<Player> firingChat = new ArrayList<Player>();

    @Override
    public void onEnable() {

        /*
         * Register the Plugin Messaging channels for bukkit for channel BungeeCord,
         * note that everything for this plugin should be sent on the FChat subchannel.
         * When a player
         */
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        this.getServer().getPluginManager().registerEvents(this, this);


        //The chat sequence will update every time a player does a chatcolor command or joins the server, NOT every 5 seconds
    }


    private void updateLocalChatSequence() {
        // Send a request to bungee to get the global player chat sequence list
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("FChat");
        out.writeUTF("requestPlayerChatSequence");
        out.writeUTF(String.valueOf(this.getServer().getPort()));

        this.getServer().sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // This follows the same procedure as bungee side.
        if (channel == "BungeeCord") {
            ByteArrayDataInput dataIn = ByteStreams.newDataInput(message);
            String subChannel = dataIn.readUTF();
            if (subChannel.equals("FChat")) {
                String command = dataIn.readUTF();
                if (command.equals("updatedPlayerChatSequence")) {
                    String serializedObject = dataIn.readUTF();

                    /*  Now that we have out serialized object encoded in UTF we have to convert it to base64 
                     *  and then deserialize it.
                     */

                    
                    byte[] rawObject = Base64.getDecoder().decode(serializedObject);
                    ByteArrayInputStream rawObjectStream = new ByteArrayInputStream(rawObject);
                    
                    HashMap<UUID, ArrayList<String>> reconstructedObject = null;

                    try {
                        ObjectInputStream objectStream = new ObjectInputStream(rawObjectStream);
                        reconstructedObject = (HashMap<UUID,ArrayList<String>>) objectStream.readObject();
                    
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    playerChatSequence = reconstructedObject;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //Update the playerChatSequence on a player join, due to a weird bug this has to be run a few ticks after the player joins
        new BukkitRunnable() {
            @Override
            public void run() {
                updateLocalChatSequence();
            }
        }.runTaskLater(this, 10);
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        //Check if that player has their UUID in the playerChatSequence
        if(playerChatSequence.containsKey(event.getPlayer().getUniqueId())) {
            if(!firingChat.contains(event.getPlayer())) { //Make sure this isn't the 2nd chat being sent
                event.setCancelled(true);
                firingChat.add(event.getPlayer());
                event.getPlayer().chat(processChat(event.getMessage(), event.getPlayer()));
            }
            else {
                //reset for next chat event
                firingChat.remove(event.getPlayer());
            }
        }
    }

    private String processChat(String in, Player player) {
        StringBuilder formatted = new StringBuilder("");    //This is the output

        for(UUID testPlayerUUID : playerChatSequence.keySet()) {
            //Search for the correct player in the playerchatsequence hashmap
            if(testPlayerUUID.equals(player.getUniqueId())) {
                ArrayList<String> targetPlayerChatSequence = playerChatSequence.get(testPlayerUUID);


                String sequence_colorSuffix = targetPlayerChatSequence.get(1);        //This will be placed after every item in the sequence
                String compactSequence = targetPlayerChatSequence.get(2);             //The actual sequence of colors(in a string)


                //turn the sequence into an arraylist
                ArrayList<String> sequence = new ArrayList<String>();
                //convert the compactSequennce into the sequence arraylist
                for(String section : compactSequence.split("\\*")) {                  //Split at the star, note the escape character because regex uses the star for zero or more
                    sequence.add(section);
                }
                
                
                //Go through each word and apply color formatting to each 
                String wordFormatted;
                int sequenceCycleIndex = 0;                                           //this will cycle through the sequence over and over again no matter how many words there are
                String[] wordArray = in.split(" ");                                   //Split on every whitespace

                for(int wordIndex = 0; wordIndex<wordArray.length; wordIndex++) {     //iterate over the words          
                    String word = wordArray[wordIndex];                          
                    wordFormatted = sequence.get(sequenceCycleIndex);                 //cycle through the sequence
                    wordFormatted += sequence_colorSuffix;                            //add the colorsuffix to the end of the  original color formatting
                    wordFormatted += word;                                            //add the word

                    //Add a space UNLESS this is the last word in the sequence
                    if(wordIndex != wordArray.length-1) {
                        wordFormatted += " ";
                    }


                    formatted.append(wordFormatted);

                    //If the sequenceCycleIndex is at the end restart at the beginning
                    sequenceCycleIndex += 1;                                          //increment
                    if(sequenceCycleIndex > sequence.size()-1) {                      //check if its bigger than the total
                        sequenceCycleIndex = 0;                                       //if so set it to zero
                    }
                }

                formatted.insert(0, targetPlayerChatSequence.get(0));                 //insert begin at beginnning of word
                formatted.append(targetPlayerChatSequence.get(3));                    //Insert end at ending of word
            }
        }
        return(formatted.toString());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("chatcolor")) {
            //Open the chatcolor inventory
            return(true);
        }        

        return(false);

    }
}
