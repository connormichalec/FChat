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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import net.milkbowl.vault.chat.Chat;

public class FChat extends JavaPlugin implements PluginMessageListener, Listener {
    public Chat chat;

    // Local cache of the playerChatSequence
    HashMap<UUID, ArrayList<String>> playerChatSequence = new HashMap<UUID, ArrayList<String>>();

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

        // Setup vault chat
        RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager()
                .getRegistration(Chat.class);
        this.chat = (Chat) chatProvider.getProvider();

        // Every 5 seconds the local chat sequence will update with the global one, as
        // well as directly after a player changes chat settings on THIS server
        updateLocalChatSequenceLoop();
    }

    private void updateLocalChatSequenceLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateLocalChatSequence();

                updateLocalChatSequenceLoop(); // recursive
            }
        }.runTaskLater(this, 100); // every 100 ticks
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
                    


                    System.out.println(reconstructedObject);
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String format = event.getFormat();
        System.out.println(format);
    }

}
