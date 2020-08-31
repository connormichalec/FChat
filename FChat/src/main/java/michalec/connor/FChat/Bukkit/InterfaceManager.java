package michalec.connor.FChat.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

public class InterfaceManager {
    public static ArrayList<CInterface> runningInterfaces = new ArrayList<CInterface>();

    public static void processInventoryEvent(InventoryClickEvent event, FChat main) {
        //Check to see if this event is on a color select interface, if it is deal with it.
        final Player player = (Player) event.getWhoClicked();

        //If that palyer has a runningInterface
        for(int interfaceIterator=0; interfaceIterator<runningInterfaces.size(); interfaceIterator++) {
            CInterface testInterface = runningInterfaces.get(interfaceIterator);
            if(testInterface.getAssociatedPlayer().getName() == player.getName()) {
                //Ok we found that this is indeed a Color selection menu interface, now just check if the player is clicking in their menu or in the colorinterface
                if(event.getClickedInventory()!=null) {
                    if(event.getClickedInventory().getType()!=InventoryType.PLAYER) {
                        //User clicked on something inside the color menu
                        
                        event.setCancelled(true);
                        if(!event.isShiftClick()) {
                            if(testInterface.processUserUpdate(event, main)) {
                                event.getWhoClicked().closeInventory();
                                main.updateLocalChatSequence();
                                interfaceIterator--;
                            }
                        }
                        else {
                            //if hes shift clicking cancel it to prevent a bug where you can take items
                        }
                    }
                }
                else {
                    //The player clicked on the space outside the menu
                }
            }
        }
    }

    //Note, when a player leaves the server this will also be called
    public static void processInventoryCloseEvent(InventoryCloseEvent event) {
        //If we find out that the inventory was closed by a player with a running interface, remove that object from runningInterfaces
        for(CInterface testInterface : runningInterfaces) {
            if(testInterface.getAssociatedPlayer().getName() == event.getPlayer().getName()) {
                runningInterfaces.remove(testInterface);
                break;
            }
        }
    }

    public static void createInterface(Player player, DataHandler dataHandler, HashMap<UUID, ArrayList<String>> playerChatSequenceCache) {
        //Create and open a new menu for a specific player
        
        for(CInterface testInterface : runningInterfaces) {
            //Clear the old interface if the user already has one open
            if(testInterface.getAssociatedPlayer().getName() == player.getName()) {
                runningInterfaces.remove(testInterface);
                break;
            }

        }

        runningInterfaces.add(new CInterface(player, dataHandler, playerChatSequenceCache));
    }
    
}