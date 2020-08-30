package michalec.connor.FChat.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

//The reason a lot of this code works: https://www.geeksforgeeks.org/g-fact-31-java-is-strictly-pass-by-value/, https://stackoverflow.com/questions/30444722/static-variable-pass-by-reference-in-java

public class CInterface {
    
    private Player associatedPlayer;
    private DataHandler dataHandler;

    private ArrayList<String> format = new ArrayList<String>();             //contains all currently selected format options

    private Inventory interfaceInventory;

    /*User limits:*/
    private int maxColors = 0;                                              //Max colors the player can use
    private Boolean canUseItalic = false;                                   //Can the user use italic formatting
    private Boolean canUseUnderline = false;                                //Can the user use underline formatting
    private Boolean canUseBold = false;                                     //Can the user use italic formatting
    private Boolean canUseMagic = false;                                    //Can the user use italic formatting
    /*                                              */

    /*Interface runtime variables:      (These variables are for keeping track of things)*/
    private int currentlyUsedColors = 0;
    /*                                              */ 

    public CInterface(Player player, DataHandler dataHandler, HashMap<UUID, ArrayList<String>> playerChatSequenceCache) {


        associatedPlayer = player;

        this.dataHandler = dataHandler;


        
        for(int i=0; i<4; i++) 
            format.add(""); //get format ready for begin, sequence_colorSuffix, sequence, and close

        //Update to reflect whats already stored previously in playerChatSequence, if the player does have data already in there
        if(playerChatSequenceCache.containsKey(player.getUniqueId()))
            format = playerChatSequenceCache.get(player.getUniqueId());

        /*Update the runtime vars to reflect the current format*/
        
        //COLORS
        currentlyUsedColors = format.get(2).replaceAll("§", "").split("\\*").length;            //note the Regex escape


        //FORMATTING
        //Magic format
        //Check to see if begin and close match §5§k+§r
        if(format.get(0).equals("§5§k+§r") && format.get(3).equals("§5§k+§r")) 
            CInterfaceTemplateItems.magicFormat.setSelection(true);
        
        //Other formats
        for(String formatCode : format.get(1).split("§")) {
            if(formatCode.equals("l"))
                CInterfaceTemplateItems.boldFormat.setSelection(true);
            else if(formatCode.equals("n"))
                CInterfaceTemplateItems.underlineFormat.setSelection(true);
            else if(formatCode.equals("o"))
                CInterfaceTemplateItems.italicFormat.setSelection(true);;
        }

        
        /*                                          */

        //assign permissive variables
        if(player.hasPermission("fchat.chatcolor.format.bold"))
            canUseBold = true;
        if(player.hasPermission("fchat.chatcolor.format.italic"))
            canUseItalic = true;
        if(player.hasPermission("fchat.chatcolor.format.underline"))
            canUseUnderline = true;
        if(player.hasPermission("fchat.chatcolor.format.magic"))
            canUseMagic = true;

        //get the amount of a colors they can use:
        if(FChat.getIfHasBasePerm(player, "fchat.chatcolor.colors")) {
            maxColors = FChat.getIntPermAttachment(player, "fchat.chatcolor.colors");
        }

        interfaceInventory = Bukkit.createInventory(null, 54, 
            FChat.colFormat(this.dataHandler.getYAMLStringField("config", "ChatColorMenuTitle").replaceAll("%COLORS_AVAILABLE%", String.valueOf(maxColors - currentlyUsedColors))));

        initialInventoryCall();
    }

    public void processUserUpdate(InventoryClickEvent event) {
        //Called when a user interacts with this interface
        if(event.isLeftClick()) {
            //This is for adding color/formats and applying state changes
            //If it can be added, update the interface

            //TODO: remove having to convert allcolorsavail and allformatsavail to lists twice
            CInterfaceItem clickedInterfaceItem = CInterfaceTemplateItems.getCInterfaceItem(event.getSlot());
            if(clickedInterfaceItem != null) {                                                                      //make sure its a valid menu object(not filler)
                if(Arrays.asList(CInterfaceTemplateItems.allColorsAvailItems).contains(clickedInterfaceItem)
                 || Arrays.asList(CInterfaceTemplateItems.allFormatsAvailItems).contains(clickedInterfaceItem))     //make sure its a color/format
                if(!clickedInterfaceItem.isCurrentlySelected()) {                                                   //We can only add a color/format that is currently not selected
                    if(Arrays.asList(CInterfaceTemplateItems.allColorsAvailItems).contains(clickedInterfaceItem)) { //is it a color or format
                        //color
                        if(currentlyUsedColors<maxColors) {                                                         //If there is actually colors available to add
                            currentlyUsedColors++;
                            format = addColorCodeToSequence(format, clickedInterfaceItem.getAssociatedColorCode());
                            clickedInterfaceItem.setSelection(true);
                        }
                    }
                    else {
                        //format
                        clickedInterfaceItem.setSelection(true);
                    }

                    updateInterface();
                }
            }
        }
        else {
            //This is for removing color/formats
            //If it can be removed, update the interface

            //TODO: remove having to convert allcolorsavail and allformatsavail to lists twice
            CInterfaceItem clickedInterfaceItem = CInterfaceTemplateItems.getCInterfaceItem(event.getSlot());
            if(clickedInterfaceItem != null) {                                                                          //make sure its a valid menu object(not filler)
                if(Arrays.asList(CInterfaceTemplateItems.allColorsAvailItems).contains(clickedInterfaceItem)
                 || Arrays.asList(CInterfaceTemplateItems.allFormatsAvailItems).contains(clickedInterfaceItem))     //make sure its a color/formatt
                if(clickedInterfaceItem.isCurrentlySelected()) {                                                        //We can only remove a color/format that is currently selected
                    if(Arrays.asList(CInterfaceTemplateItems.allColorsAvailItems).contains(clickedInterfaceItem)) {     //is it a color or format
                        //color
                        currentlyUsedColors--;
                        format = removeColorCodeFromSequence(format, clickedInterfaceItem.getAssociatedColorCode());
                        clickedInterfaceItem.setSelection(false);
                    }
                    else {
                        //format
                        clickedInterfaceItem.setSelection(false);
                    }

                    updateInterface();
                }
            }
        }

        System.out.println(format);

    }

    public void updateInterface() {
        //Update the interface itself

        //SET COLORS
        if(currentlyUsedColors<maxColors) {
            for(CInterfaceItem interfaceItem : CInterfaceTemplateItems.allColorsAvailItems) {
                interfaceInventory.setItem(interfaceItem.getSlot(), interfaceItem.getItem());
            }
        }
        else {
            for(CInterfaceItem interfaceItem : CInterfaceTemplateItems.noMoreColorsAvailItems) {
                interfaceInventory.setItem(interfaceItem.getSlot(), interfaceItem.getItem());
            }
        }
        //add enchant glow to the colors that are currently being used(removing glow from ones that dont), then add them to the itnerface where they are suppsoed to be:
        for(CInterfaceItem item : CInterfaceTemplateItems.allColorsAvailItems) {
            if(item.isCurrentlySelected()) {
                CInterfaceTemplateItems.addMetaEnchantGlow(item);
                interfaceInventory.setItem(item.getSlot(), item.getItem());
            }
            else {
                CInterfaceTemplateItems.removeMetaEnchantGlow(item);
                //Only set the unselected item if there is more colors available
                if(currentlyUsedColors<maxColors)
                    interfaceInventory.setItem(item.getSlot(), item.getItem());
            }
        }
        
        /*SET FORMATS    TODO: Make a method to make this process shorter*/
        if(canUseItalic) {
            if(CInterfaceTemplateItems.italicFormat.isCurrentlySelected()) {
                CInterfaceTemplateItems.addMetaEnchantGlow(CInterfaceTemplateItems.italicFormat);
            }
            else {
                CInterfaceTemplateItems.removeMetaEnchantGlow(CInterfaceTemplateItems.italicFormat);                
            }

            interfaceInventory.setItem(CInterfaceTemplateItems.italicFormat.getSlot(), CInterfaceTemplateItems.italicFormat.getItem());
        }

        if(canUseMagic) {
            if(CInterfaceTemplateItems.magicFormat.isCurrentlySelected()) {
                CInterfaceTemplateItems.addMetaEnchantGlow(CInterfaceTemplateItems.magicFormat);
            }
            else {
                CInterfaceTemplateItems.removeMetaEnchantGlow(CInterfaceTemplateItems.magicFormat);
            }

            interfaceInventory.setItem(CInterfaceTemplateItems.magicFormat.getSlot(), CInterfaceTemplateItems.magicFormat.getItem());
        }

        if(canUseBold) {
            if(CInterfaceTemplateItems.boldFormat.isCurrentlySelected()) {
                CInterfaceTemplateItems.addMetaEnchantGlow(CInterfaceTemplateItems.boldFormat);
            }
            else {
                CInterfaceTemplateItems.removeMetaEnchantGlow(CInterfaceTemplateItems.boldFormat);
            }

            interfaceInventory.setItem(CInterfaceTemplateItems.boldFormat.getSlot(), CInterfaceTemplateItems.boldFormat.getItem());
        }

        if(canUseUnderline) {
            if(CInterfaceTemplateItems.underlineFormat.isCurrentlySelected()) {
                CInterfaceTemplateItems.addMetaEnchantGlow(CInterfaceTemplateItems.underlineFormat);
            }
            else {
                CInterfaceTemplateItems.removeMetaEnchantGlow(CInterfaceTemplateItems.underlineFormat);
            }

            interfaceInventory.setItem(CInterfaceTemplateItems.underlineFormat.getSlot(), CInterfaceTemplateItems.underlineFormat.getItem());
        }
        /*              */

        interfaceInventory.setItem(CInterfaceTemplateItems.preview.getSlot(), CInterfaceTemplateItems.setMetaTitle(CInterfaceTemplateItems.preview,
            "&r"+FChat.processChat(dataHandler.getYAMLStringField("config", "GUI_Buttons.PreviewText"), format)).getItem());
    }
    
    public void initialInventoryCall() {
        //This is will get called once when the inventory needs to open, and will construct the inventory, other events by the user will cause the inventroy to get edited

        //SET STATE NAVIGATION
        for(CInterfaceItem interfaceItem : CInterfaceTemplateItems.stateSaveItems) {
            interfaceInventory.setItem(interfaceItem.getSlot(), interfaceItem.getItem());
        }

        //FILLER
        for(CInterfaceItem interfaceItem : CInterfaceTemplateItems.fillerItems) {
            interfaceInventory.setItem(interfaceItem.getSlot(), interfaceItem.getItem());
        }

        //SET COLORS
        if(currentlyUsedColors<maxColors) {
            for(CInterfaceItem interfaceItem : CInterfaceTemplateItems.allColorsAvailItems) {
                interfaceInventory.setItem(interfaceItem.getSlot(), interfaceItem.getItem());
            }
        }
        else {
            for(CInterfaceItem interfaceItem : CInterfaceTemplateItems.noMoreColorsAvailItems) {
                interfaceInventory.setItem(interfaceItem.getSlot(), interfaceItem.getItem());
            }
        }
        //add enchant glow to the colors that are currently being used, then add them to the itnerface where they are suppsoed to be:
        ArrayList<CInterfaceItem> usedColors = CInterfaceTemplateItems.setCInterfaceItemColorsCurrentlyBeingUsed(format.get(2));
        for(CInterfaceItem item : usedColors) {
            CInterfaceTemplateItems.addMetaEnchantGlow(item);
            interfaceInventory.setItem(item.getSlot(), item.getItem());
        }
        
        /*SET FORMATS    TODO: Make a method to make this process shorter*/
        if(canUseItalic) {
            if(CInterfaceTemplateItems.italicFormat.isCurrentlySelected()) {
                CInterfaceTemplateItems.italicFormat.setSelection(true);
                CInterfaceTemplateItems.addMetaEnchantGlow(CInterfaceTemplateItems.italicFormat);
            }

            interfaceInventory.setItem(CInterfaceTemplateItems.italicFormat.getSlot(), CInterfaceTemplateItems.italicFormat.getItem());
        }
        else {
            interfaceInventory = (cantUseFormatItem(interfaceInventory, CInterfaceTemplateItems.italicFormat));
        }

        if(canUseBold) {
            if(CInterfaceTemplateItems.boldFormat.isCurrentlySelected()) {
                CInterfaceTemplateItems.boldFormat.setSelection(true);
                CInterfaceTemplateItems.addMetaEnchantGlow(CInterfaceTemplateItems.boldFormat);
            }

            interfaceInventory.setItem(CInterfaceTemplateItems.boldFormat.getSlot(), CInterfaceTemplateItems.boldFormat.getItem());
        }
        else {
            interfaceInventory = (cantUseFormatItem(interfaceInventory, CInterfaceTemplateItems.boldFormat));
        }

        if(canUseMagic) {
            if(CInterfaceTemplateItems.magicFormat.isCurrentlySelected()) {
                CInterfaceTemplateItems.magicFormat.setSelection(true);
                CInterfaceTemplateItems.addMetaEnchantGlow(CInterfaceTemplateItems.magicFormat);
            }

            interfaceInventory.setItem(CInterfaceTemplateItems.magicFormat.getSlot(), CInterfaceTemplateItems.magicFormat.getItem());
        }
        else {
            interfaceInventory = (cantUseFormatItem(interfaceInventory, CInterfaceTemplateItems.magicFormat));
        }

        if(canUseUnderline) {
            if(CInterfaceTemplateItems.underlineFormat.isCurrentlySelected()) {
                CInterfaceTemplateItems.underlineFormat.setSelection(true);
                CInterfaceTemplateItems.addMetaEnchantGlow(CInterfaceTemplateItems.underlineFormat);
            }

            interfaceInventory.setItem(CInterfaceTemplateItems.underlineFormat.getSlot(), CInterfaceTemplateItems.underlineFormat.getItem());
        }
        else {
            interfaceInventory = (cantUseFormatItem(interfaceInventory, CInterfaceTemplateItems.underlineFormat));
        }
        /*              */

        interfaceInventory.setItem(CInterfaceTemplateItems.preview.getSlot(), CInterfaceTemplateItems.setMetaTitle(CInterfaceTemplateItems.preview,
            "&r"+FChat.processChat(dataHandler.getYAMLStringField("config", "GUI_Buttons.PreviewText"), format)).getItem());

        associatedPlayer.openInventory(interfaceInventory);
    }

    public Player getAssociatedPlayer() {
        return(associatedPlayer);
    }

    private void resetButton() {
        currentlyUsedColors = 0;

        for(CInterfaceItem item : CInterfaceTemplateItems.allColorsAvailItems)
            item.setSelection(false);
        for(CInterfaceItem item : CInterfaceTemplateItems.allFormatsAvailItems)
            item.setSelection(false);

        for(int i = 0; i<format.size(); i++)
            format.set(i, "");
    }

    private Inventory cantUseFormatItem(Inventory inventoryIn, CInterfaceItem original) {
        ItemStack item = CInterfaceTemplateItems.cloneInterfaceItem(CInterfaceTemplateItems.cantUseFormatBaseItem, original.getSlot()).getItem();
        Inventory inventoryOut = inventoryIn;
        inventoryOut.setItem(original.getSlot(), item);
        return(inventoryOut);
    }

    private ArrayList<String> removeColorCodeFromSequence(ArrayList<String> in, String removal) {
        in.set(2, in.get(2).replaceAll("§"+removal+"\\*", ""));     //NOTE REGEX escape
        return(in);
    }

    private ArrayList<String> addColorCodeToSequence(ArrayList<String> in, String add) {
        in.set(2, in.get(2)+"§"+add+"*");
        return(in);
    }


    
    
}