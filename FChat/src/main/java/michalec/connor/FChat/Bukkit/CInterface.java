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
    private CInterfaceTemplateItems cInterfaceTemplateItems;
    
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
        cInterfaceTemplateItems = new CInterfaceTemplateItems();

        cInterfaceTemplateItems.initializeData(dataHandler);

        associatedPlayer = player;

        this.dataHandler = dataHandler;



        //Update to reflect whats already stored previously in playerChatSequence, if the player does have data already in there
        if(playerChatSequenceCache.containsKey(player.getUniqueId())) {
            format.addAll(playerChatSequenceCache.get(player.getUniqueId()));
        }
        else {
            for(int i=0; i<4; i++) 
                format.add(""); //empty for begin, sequence_colorSuffix, sequence, and close
        }

        /*Update the runtime vars to reflect the current format*/
        
        //COLORS
        if(format.get(2).length() == 0) {
            currentlyUsedColors = 0;
        }
        else {
            currentlyUsedColors = format.get(2).replaceAll("§", "").split("\\*").length;            //note the Regex escape
        }


        //FORMATTING
        //Magic format
        //Check to see if begin and close match §5§k+§r
        if(format.get(0).equals("§5§k+§r") && format.get(3).equals("§5§k+§r")) 
            cInterfaceTemplateItems.magicFormat.setSelection(true);
        
        //Other formats
        for(String formatCode : format.get(1).split("§")) {
            if(formatCode.equals("l"))
                cInterfaceTemplateItems.boldFormat.setSelection(true);
            else if(formatCode.equals("n"))
                cInterfaceTemplateItems.underlineFormat.setSelection(true);
            else if(formatCode.equals("o"))
                cInterfaceTemplateItems.italicFormat.setSelection(true);;
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

    public boolean processUserUpdate(InventoryClickEvent event, FChat main) {
        //Called when a user interacts with this interface
        if(event.isLeftClick()) {
            //This is for adding color/formats and applying state changes
            //If it can be added, update the interface

            //TODO: remove having to convert allcolorsavail and allformatsavail to lists twice
            CInterfaceItem clickedInterfaceItem = cInterfaceTemplateItems.getCInterfaceItem(event.getSlot());
            if(clickedInterfaceItem != null) {                                                                      //make sure its a valid menu object(not filler)
                if(Arrays.asList(cInterfaceTemplateItems.allColorsAvailItems).contains(clickedInterfaceItem)
                 || Arrays.asList(cInterfaceTemplateItems.allFormatsAvailItems).contains(clickedInterfaceItem)) {     //make sure its a color/format
                    if(!clickedInterfaceItem.isCurrentlySelected()) {                                                   //We can only add a color/format that is currently not selected
                        if(Arrays.asList(cInterfaceTemplateItems.allColorsAvailItems).contains(clickedInterfaceItem)) { //is it a color or format
                            //color
                            if(currentlyUsedColors<maxColors) {                                                         //If there is actually colors available to add
                                currentlyUsedColors++;
                                format = addColorCodeToSequence(format, clickedInterfaceItem.getAssociatedColorCode());
                                clickedInterfaceItem.setSelection(true);
                            }
                        }
                        else {
                            //format
                            if(clickedInterfaceItem.getAssociatedColorCode().equals("§5§k+§r") && canUseMagic) {
                                //magic
                                format.set(0, format.get(0)+"§5§k+§r");
                                format.set(3, format.get(3)+"§5§k+§r");
                                clickedInterfaceItem.setSelection(true);
                            }
                            else {
                                if(clickedInterfaceItem.getAssociatedColorCode().equals("l") && canUseBold) {
                                    format = addFormatCodeToSequence(format, "l");
                                    clickedInterfaceItem.setSelection(true);
                                }
                                else if(clickedInterfaceItem.getAssociatedColorCode().equals("o") && canUseItalic) {
                                    format = addFormatCodeToSequence(format, "o");
                                    clickedInterfaceItem.setSelection(true);
                                }
                                else if(clickedInterfaceItem.getAssociatedColorCode().equals("n") && canUseUnderline) {
                                    format = addFormatCodeToSequence(format, "n");
                                    clickedInterfaceItem.setSelection(true);
                                }
                            }
                        }

                        updateInterface();
                    }
                }
                else {
                    //Ok it's another button
                    if(clickedInterfaceItem.getSlot() == cInterfaceTemplateItems.reset.getSlot()) {
                        //If it's the reset button
                        resetButton();
                    }
                    else if(clickedInterfaceItem.getSlot() == cInterfaceTemplateItems.cancel.getSlot()) {
                        //If it's the cancel button
                        return(true); //exit
                    }
                    else if(clickedInterfaceItem.getSlot() == cInterfaceTemplateItems.applyChanges.getSlot()) {
                        //If it's the apply changes button
                        main.updatePlayersColor((Player) event.getWhoClicked(), format.get(0), format.get(1), format.get(2), format.get(3));
                        return(true); //exit
                    }
                    updateInterface();
                }
            }

        }
        else {
            //This is for removing color/formats
            //If it can be removed, update the interface

            //TODO: remove having to convert allcolorsavail and allformatsavail to lists twice
            CInterfaceItem clickedInterfaceItem = cInterfaceTemplateItems.getCInterfaceItem(event.getSlot());
            if(clickedInterfaceItem != null) {                                                                          //make sure its a valid menu object(not filler)
                if(Arrays.asList(cInterfaceTemplateItems.allColorsAvailItems).contains(clickedInterfaceItem)
                 || Arrays.asList(cInterfaceTemplateItems.allFormatsAvailItems).contains(clickedInterfaceItem)) {       //make sure its a color/formatt
                    if(clickedInterfaceItem.isCurrentlySelected()) {                                                        //We can only remove a color/format that is currently selected
                        if(Arrays.asList(cInterfaceTemplateItems.allColorsAvailItems).contains(clickedInterfaceItem)) {     //is it a color or format
                            //color
                            currentlyUsedColors--;
                            format = removeColorCodeFromSequence(format, clickedInterfaceItem.getAssociatedColorCode());
                            clickedInterfaceItem.setSelection(false);
                        }
                        else {
                            //format - permission check is unnecessary but im doing it anyway
                            if(clickedInterfaceItem.getAssociatedColorCode().equals("§5§k+§r") && canUseMagic) {
                                //magic
                                format.set(0, format.get(0).replaceAll("§5§k\\+§r", ""));//NOTE REGEX ESCAPE
                                format.set(3, format.get(3).replaceAll("§5§k\\+§r", ""));//NOTE REGEX ESCAPE;
                                clickedInterfaceItem.setSelection(false);
                            }
                            else {
                                if(clickedInterfaceItem.getAssociatedColorCode().equals("l") && canUseBold) {
                                    format = removeFormatCodeFromSequence(format, "l");
                                    clickedInterfaceItem.setSelection(false);
                                }
                                else if(clickedInterfaceItem.getAssociatedColorCode().equals("o") && canUseItalic) {
                                    format = removeFormatCodeFromSequence(format, "o");
                                    clickedInterfaceItem.setSelection(false);
                                }
                                else if(clickedInterfaceItem.getAssociatedColorCode().equals("n") && canUseUnderline) {
                                    format = removeFormatCodeFromSequence(format, "n");
                                    clickedInterfaceItem.setSelection(false);
                                }
                            }
                        }

                        updateInterface();
                    }
                }
            }
        }

        return(false);  //dont exit


    }

    public void updateInterface() {
        //Update the interface itself

        //SET COLORS
        if(currentlyUsedColors<maxColors) {
            for(CInterfaceItem interfaceItem : cInterfaceTemplateItems.allColorsAvailItems) {
                interfaceInventory.setItem(interfaceItem.getSlot(), interfaceItem.getItem());
            }
        }
        else {
            for(CInterfaceItem interfaceItem : cInterfaceTemplateItems.noMoreColorsAvailItems) {
                interfaceInventory.setItem(interfaceItem.getSlot(), interfaceItem.getItem());
            }
        }
        //add enchant glow to the colors that are currently being used(removing glow from ones that dont), then add them to the itnerface where they are suppsoed to be:
        for(CInterfaceItem item : cInterfaceTemplateItems.allColorsAvailItems) {
            if(item.isCurrentlySelected()) {
                cInterfaceTemplateItems.addMetaEnchantGlow(item);
                interfaceInventory.setItem(item.getSlot(), item.getItem());
            }
            else {
                cInterfaceTemplateItems.removeMetaEnchantGlow(item);
                //Only set the unselected item if there is more colors available
                if(currentlyUsedColors<maxColors)
                    interfaceInventory.setItem(item.getSlot(), item.getItem());
            }
        }
        
        /*SET FORMATS    TODO: Make a method to make this process shorter*/
        if(canUseItalic) {
            if(cInterfaceTemplateItems.italicFormat.isCurrentlySelected()) {
                cInterfaceTemplateItems.addMetaEnchantGlow(cInterfaceTemplateItems.italicFormat);
            }
            else {
                cInterfaceTemplateItems.removeMetaEnchantGlow(cInterfaceTemplateItems.italicFormat);                
            }

            interfaceInventory.setItem(cInterfaceTemplateItems.italicFormat.getSlot(), cInterfaceTemplateItems.italicFormat.getItem());
        }

        if(canUseMagic) {
            if(cInterfaceTemplateItems.magicFormat.isCurrentlySelected()) {
                cInterfaceTemplateItems.addMetaEnchantGlow(cInterfaceTemplateItems.magicFormat);
            }
            else {
                cInterfaceTemplateItems.removeMetaEnchantGlow(cInterfaceTemplateItems.magicFormat);
            }

            interfaceInventory.setItem(cInterfaceTemplateItems.magicFormat.getSlot(), cInterfaceTemplateItems.magicFormat.getItem());
        }

        if(canUseBold) {
            if(cInterfaceTemplateItems.boldFormat.isCurrentlySelected()) {
                cInterfaceTemplateItems.addMetaEnchantGlow(cInterfaceTemplateItems.boldFormat);
            }
            else {
                cInterfaceTemplateItems.removeMetaEnchantGlow(cInterfaceTemplateItems.boldFormat);
            }

            interfaceInventory.setItem(cInterfaceTemplateItems.boldFormat.getSlot(), cInterfaceTemplateItems.boldFormat.getItem());
        }

        if(canUseUnderline) {
            if(cInterfaceTemplateItems.underlineFormat.isCurrentlySelected()) {
                cInterfaceTemplateItems.addMetaEnchantGlow(cInterfaceTemplateItems.underlineFormat);
            }
            else {
                cInterfaceTemplateItems.removeMetaEnchantGlow(cInterfaceTemplateItems.underlineFormat);
            }

            interfaceInventory.setItem(cInterfaceTemplateItems.underlineFormat.getSlot(), cInterfaceTemplateItems.underlineFormat.getItem());
        }
        /*              */

        interfaceInventory.setItem(cInterfaceTemplateItems.preview.getSlot(), cInterfaceTemplateItems.setMetaTitle(cInterfaceTemplateItems.preview,
            "&r"+FChat.processChat(dataHandler.getYAMLStringField("config", "GUI_Buttons.PreviewText"), format)).getItem());
    }
    
    public void initialInventoryCall() {
        //This is will get called once when the inventory needs to open, and will construct the inventory, other events by the user will cause the inventroy to get edited

        //SET STATE NAVIGATION
        for(CInterfaceItem interfaceItem : cInterfaceTemplateItems.stateSaveItems) {
            interfaceInventory.setItem(interfaceItem.getSlot(), interfaceItem.getItem());
        }

        //FILLER
        for(CInterfaceItem interfaceItem : cInterfaceTemplateItems.fillerItems) {
            interfaceInventory.setItem(interfaceItem.getSlot(), interfaceItem.getItem());
        }

        //SET COLORS
        if(currentlyUsedColors<maxColors) {
            for(CInterfaceItem interfaceItem : cInterfaceTemplateItems.allColorsAvailItems) {
                interfaceInventory.setItem(interfaceItem.getSlot(), interfaceItem.getItem());
            }
        }
        else {
            for(CInterfaceItem interfaceItem : cInterfaceTemplateItems.noMoreColorsAvailItems) {
                interfaceInventory.setItem(interfaceItem.getSlot(), interfaceItem.getItem());
            }
        }
        //add enchant glow to the colors that are currently being used, then add them to the itnerface where they are suppsoed to be:
        ArrayList<CInterfaceItem> usedColors = cInterfaceTemplateItems.setCInterfaceItemColorsCurrentlyBeingUsed(format.get(2));
        for(CInterfaceItem item : usedColors) {
            cInterfaceTemplateItems.addMetaEnchantGlow(item);
            interfaceInventory.setItem(item.getSlot(), item.getItem());
        }
        
        /*SET FORMATS    TODO: Make a method to make this process shorter*/
        if(canUseItalic) {
            if(cInterfaceTemplateItems.italicFormat.isCurrentlySelected()) {
                cInterfaceTemplateItems.italicFormat.setSelection(true);
                cInterfaceTemplateItems.addMetaEnchantGlow(cInterfaceTemplateItems.italicFormat);
            }

            interfaceInventory.setItem(cInterfaceTemplateItems.italicFormat.getSlot(), cInterfaceTemplateItems.italicFormat.getItem());
        }
        else {
            interfaceInventory = (cantUseFormatItem(interfaceInventory, cInterfaceTemplateItems.italicFormat));
        }

        if(canUseBold) {
            if(cInterfaceTemplateItems.boldFormat.isCurrentlySelected()) {
                cInterfaceTemplateItems.boldFormat.setSelection(true);
                cInterfaceTemplateItems.addMetaEnchantGlow(cInterfaceTemplateItems.boldFormat);
            }

            interfaceInventory.setItem(cInterfaceTemplateItems.boldFormat.getSlot(), cInterfaceTemplateItems.boldFormat.getItem());
        }
        else {
            interfaceInventory = (cantUseFormatItem(interfaceInventory, cInterfaceTemplateItems.boldFormat));
        }

        if(canUseMagic) {
            if(cInterfaceTemplateItems.magicFormat.isCurrentlySelected()) {
                cInterfaceTemplateItems.magicFormat.setSelection(true);
                cInterfaceTemplateItems.addMetaEnchantGlow(cInterfaceTemplateItems.magicFormat);
            }

            interfaceInventory.setItem(cInterfaceTemplateItems.magicFormat.getSlot(), cInterfaceTemplateItems.magicFormat.getItem());
        }
        else {
            interfaceInventory = (cantUseFormatItem(interfaceInventory, cInterfaceTemplateItems.magicFormat));
        }

        if(canUseUnderline) {
            if(cInterfaceTemplateItems.underlineFormat.isCurrentlySelected()) {
                cInterfaceTemplateItems.underlineFormat.setSelection(true);
                cInterfaceTemplateItems.addMetaEnchantGlow(cInterfaceTemplateItems.underlineFormat);
            }

            interfaceInventory.setItem(cInterfaceTemplateItems.underlineFormat.getSlot(), cInterfaceTemplateItems.underlineFormat.getItem());
        }
        else {
            interfaceInventory = (cantUseFormatItem(interfaceInventory, cInterfaceTemplateItems.underlineFormat));
        }
        /*              */

        interfaceInventory.setItem(cInterfaceTemplateItems.preview.getSlot(), cInterfaceTemplateItems.setMetaTitle(cInterfaceTemplateItems.preview,
            "&r"+FChat.processChat(dataHandler.getYAMLStringField("config", "GUI_Buttons.PreviewText"), format)).getItem());

        associatedPlayer.openInventory(interfaceInventory);
    }

    public Player getAssociatedPlayer() {
        return(associatedPlayer);
    }

    private void resetButton() {
        currentlyUsedColors = 0;

        for(CInterfaceItem item : cInterfaceTemplateItems.allColorsAvailItems)
            item.setSelection(false);
        for(CInterfaceItem item : cInterfaceTemplateItems.allFormatsAvailItems)
            item.setSelection(false);

        for(int i = 0; i<format.size(); i++)
            format.set(i, "");
    }

    private Inventory cantUseFormatItem(Inventory inventoryIn, CInterfaceItem original) {
        ItemStack item = cInterfaceTemplateItems.cloneInterfaceItem(cInterfaceTemplateItems.cantUseFormatBaseItem, original.getSlot()).getItem();
        Inventory inventoryOut = inventoryIn;
        inventoryOut.setItem(original.getSlot(), item);
        return(inventoryOut);
    }

    private ArrayList<String> removeColorCodeFromSequence(ArrayList<String> in, String removal) {
        in.set(2, in.get(2).replaceAll("§"+removal+"\\*", ""));     //NOTE REGEX escape
        return(in);
    }

    private ArrayList<String> removeFormatCodeFromSequence(ArrayList<String> in, String removal) {
        in.set(1, in.get(1).replaceAll("§"+removal, ""));
        return(in);
    }

    private ArrayList<String> addColorCodeToSequence(ArrayList<String> in, String add) {
        in.set(2, in.get(2)+"§"+add+"*");
        return(in);
    }

    private ArrayList<String> addFormatCodeToSequence(ArrayList<String> in, String add) {
        in.set(1, format.get(1)+"§"+add);
        return(in);
    }


    
    
}