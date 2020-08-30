package michalec.connor.FChat.Bukkit;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CInterfaceTemplateItems {
    //This class contains all template items for CInterface
   
    /*Template items:                   (These are items that are to be used for certain         buttons in the interface*/
    public static CInterfaceItem filler = new CInterfaceItem(
        new ItemStack(Material.GRAY_STAINED_GLASS_PANE), 0);                     //Fill unused space in the interface

    public static CInterfaceItem preview = new CInterfaceItem(
        new ItemStack(Material.END_CRYSTAL), 40);                                //A preview of the chatcolor

    public static CInterfaceItem noPermColorBaseItem = new CInterfaceItem(
        new ItemStack(Material.GRAY_DYE), 0);                                    //If a user has run out of colors, note this will have many instances

    public static CInterfaceItem cantUseFormatBaseItem = new CInterfaceItem(
        new ItemStack(Material.GRAY_DYE), 0);                                    //If a user cannot use this format

    public static CInterfaceItem applyChanges = new CInterfaceItem(
        new ItemStack(Material.LIME_STAINED_GLASS_PANE), 49);                    //For when a user wants to apply changes to their chat

    public static CInterfaceItem cancel = new CInterfaceItem(
        new ItemStack(Material.BARRIER), 45);                                    //For when a user wants to cancel and close the inventory

    public static CInterfaceItem reset = new CInterfaceItem(
        new ItemStack(Material.RED_STAINED_GLASS_PANE), 53);                     //For when a user wants to reset their chatcolor to the default(will just send empty strings which will automatically get handled by bungee side) 

    public static CInterfaceItem blackColor = new CInterfaceItem("0",
        new ItemStack(Material.BLACK_CONCRETE), 1);                              //For black chatcolor(0)

    public static CInterfaceItem dark_blueColor = new CInterfaceItem("1",
        new ItemStack(Material.BLUE_CONCRETE), 2);                               //For dark blue chatcolor(1)

    public static CInterfaceItem dark_greenColor = new CInterfaceItem("2",
        new ItemStack(Material.GREEN_CONCRETE), 3);                              //For green chatcolor(2)

    public static CInterfaceItem cyanColor = new CInterfaceItem("3",
        new ItemStack(Material.CYAN_CONCRETE), 4);                               //For cyan chatcolor(3)

    public static CInterfaceItem dark_redColor = new CInterfaceItem("4",
        new ItemStack(Material.RED_CONCRETE), 5);                                //For dark red chatcolor(4)

    public static CInterfaceItem purpleColor = new CInterfaceItem("5",
        new ItemStack(Material.PURPLE_CONCRETE), 6);                             //For purple chatcolor(5)

    public static CInterfaceItem orangeColor = new CInterfaceItem("6",
        new ItemStack(Material.ORANGE_CONCRETE), 7);                             //For orange chatcolor(6)

    public static CInterfaceItem light_grayColor = new CInterfaceItem("7",
        new ItemStack(Material.LIGHT_GRAY_CONCRETE), 10);                        //For light gray chatcolor(7)

    public static CInterfaceItem dark_grayColor = new CInterfaceItem("8",
        new ItemStack(Material.GRAY_CONCRETE), 11);                              //For dark gray chatcolor(8)

    public static CInterfaceItem light_blueColor = new CInterfaceItem("9",
        new ItemStack(Material.LIGHT_BLUE_CONCRETE), 12);                        //For light blue chatcolor(9)

    public static CInterfaceItem light_greenColor = new CInterfaceItem("a",
        new ItemStack(Material.LIME_CONCRETE), 13);                              //For light green chatcolor(a)

    public static CInterfaceItem aquaColor = new CInterfaceItem("b",
        new ItemStack(Material.LIGHT_BLUE_CONCRETE_POWDER), 14);                 //For aqua chatcolor(b)

    public static CInterfaceItem light_redColor = new CInterfaceItem("c",
        new ItemStack(Material.RED_CONCRETE_POWDER), 15);                        //For light red chatcolor(c)

    public static CInterfaceItem pinkColor = new CInterfaceItem("d",
        new ItemStack(Material.PINK_CONCRETE), 16);                              //For pink chatcolor(d)

    public static CInterfaceItem yellowColor = new CInterfaceItem("e",
        new ItemStack(Material.YELLOW_CONCRETE), 22);                            //For yellow chatcolor(e)

    public static CInterfaceItem magicFormat = new CInterfaceItem(
        new ItemStack(Material.CHORUS_PLANT), 37);                               //For magic format(k)

    public static CInterfaceItem boldFormat = new CInterfaceItem(
        new ItemStack(Material.IRON_BLOCK), 38);                                 //For bold format(l)

    public static CInterfaceItem italicFormat = new CInterfaceItem(
        new ItemStack(Material.STICK), 42);                                      //For italic format(o)

    public static CInterfaceItem underlineFormat = new CInterfaceItem(
        new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE), 43);              //For underline format(n)



    //COLLECTION DECLARATION
    public static CInterfaceItem[] stateSaveItems;
    public static CInterfaceItem[] noMoreColorsAvailItems;
    public static CInterfaceItem[] allColorsAvailItems;
    //NOTE CURRENTLY UNUSED: REMOVE TODO?
    public static CInterfaceItem[] cantUseAnyFormat;
    public static CInterfaceItem[] allFormatsAvailItems;
    
    //ALL SLOTS USED BY ANY MENU ITEM(except filler):
    public static ArrayList<Integer> allUsedSlots = new ArrayList<Integer>();

    //FILLER ITEM COLLECTION:
    public static ArrayList<CInterfaceItem> fillerItems;
    



    public static void initializeData(DataHandler dataHandler) {
        //TEMPLATE METADATA(This can change later to adapt to user changes and add extra lore and stuff)
        
        //NO COLOR PERMS ITEM
        setConfigMetaTitle(noPermColorBaseItem, "NotEnoughColorsAvailable", dataHandler);


        //NO FORMAT PERMS ITEM
        setConfigMetaTitle(cantUseFormatBaseItem, "CantUseFormat", dataHandler);


        //APPLY CHANGES ITEM
        setConfigMetaTitle(applyChanges, "ApplyColors", dataHandler);

        
        //CANCEL ITEM
        setConfigMetaTitle(cancel, "Cancel", dataHandler);


        //RESET COLORS ITEM
        setConfigMetaTitle(reset, "ResetColors", dataHandler);


        //FILLER ITEM
        setMetaTitle(filler, " ");

        
        //BLACK
        setMetaTitle(blackColor, "&0Black" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //BLUE
        setMetaTitle(dark_blueColor, "&1Blue" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));

        
        //GREEN
        setMetaTitle(dark_greenColor, "&2Green" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //CYAN
        setMetaTitle(cyanColor, "&3Cyan" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //RED
        setMetaTitle(dark_redColor, "&4Red" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));

        
        //PURPLE
        setMetaTitle(purpleColor, "&5Purple" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //ORANGE
        setMetaTitle(orangeColor, "&6Orange" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //LIGHT GRAY
        setMetaTitle(light_grayColor, "&7Gray" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //GRAY
        setMetaTitle(dark_grayColor, "&8Dark Gray" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //LIGHT BLUE
        setMetaTitle(light_blueColor, "&9Light Blue" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));

        
        //LIGHT GREEN
        setMetaTitle(light_greenColor, "&aLight Green" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //AQUA
        setMetaTitle(aquaColor, "&bAqua" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //LIGHT RED
        setMetaTitle(light_redColor, "&cLight Red" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //PINK
        setMetaTitle(pinkColor, "&dPink" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //YELLOW
        setMetaTitle(yellowColor, "&eYellow" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //MAGIC FORMAT
        setMetaTitle(magicFormat, "&5&k+&rMagic&5&k+" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //BOLD FORMAT
        setMetaTitle(boldFormat, "&r&lBold" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //ITALIC FORMAT
        setMetaTitle(italicFormat, "&r&oItalic" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        //UNDERLINE FORMAT
        setMetaTitle(underlineFormat, "&r&nUnderline" + dataHandler.getYAMLStringField("config", "GUI_Buttons.ColorFormatButtonInfo"));


        /*     ITEM COLLECTIONS            */

        //Basic items that make up the state save part of the menu(save reset cancel etc...), to make easier construction of the inventory
        //NOTE, DO NOT EDIT METADATA DIRECTLY TO THESE VARIABLES, THEY ARE NOT REFERENCING THE ORIGINAL VARIABLES
        stateSaveItems = new CInterfaceItem[]{cancel, applyChanges, reset};

        //All the colors
        allColorsAvailItems = new CInterfaceItem[]{blackColor, dark_blueColor, dark_greenColor,
                                              cyanColor, dark_redColor, purpleColor,
                                              orangeColor, light_grayColor, orangeColor,
                                              light_grayColor, dark_grayColor, light_blueColor,
                                              light_greenColor, aquaColor, light_redColor,
                                              pinkColor, yellowColor
        };

        //All the formats
        allFormatsAvailItems = new CInterfaceItem[]{magicFormat, boldFormat, italicFormat,
                                               underlineFormat
        };

        //A set of items that make up the menu when the user does nto have permissions to chose any more formats
        cantUseAnyFormat = new CInterfaceItem[]{cloneInterfaceItem(cantUseFormatBaseItem, 37), cloneInterfaceItem(cantUseFormatBaseItem, 38),
                                                       cloneInterfaceItem(cantUseFormatBaseItem, 42), cloneInterfaceItem(cantUseFormatBaseItem, 43)
        };

        //A set of items that make up the menu when the user does not have permissions to chose any more colors
        noMoreColorsAvailItems = new CInterfaceItem[]{cloneInterfaceItem(noPermColorBaseItem, 1), cloneInterfaceItem(noPermColorBaseItem, 2),
                                                      cloneInterfaceItem(noPermColorBaseItem, 3), cloneInterfaceItem(noPermColorBaseItem, 4),
                                                      cloneInterfaceItem(noPermColorBaseItem, 5), cloneInterfaceItem(noPermColorBaseItem, 6),
                                                      cloneInterfaceItem(noPermColorBaseItem, 7), cloneInterfaceItem(noPermColorBaseItem, 10),
                                                      cloneInterfaceItem(noPermColorBaseItem, 11), cloneInterfaceItem(noPermColorBaseItem, 12),
                                                      cloneInterfaceItem(noPermColorBaseItem, 13), cloneInterfaceItem(noPermColorBaseItem, 14),
                                                      cloneInterfaceItem(noPermColorBaseItem, 15), cloneInterfaceItem(noPermColorBaseItem, 16),
                                                      cloneInterfaceItem(noPermColorBaseItem, 22)
        };


        //Find out all the currently used clickable slots
        CInterfaceItem[] allItems = getAllUserItems();

        for(CInterfaceItem interfaceItem : allItems) {
            allUsedSlots.add(interfaceItem.getSlot());
        }


        //A set of items that make up the rest of the menu to fill it in
        fillerItems = new ArrayList<CInterfaceItem>();
        for(int i = 0; i<54; i++) {
            if(!allUsedSlots.contains(i)) {
                CInterfaceItem fillInterfaceItem = cloneInterfaceItem(filler, i);
                ItemStack fillItem = fillInterfaceItem.getItem();
                fillInterfaceItem.setItem(fillItem);
                fillerItems.add(fillInterfaceItem);
            }
        }

        //set all colors to be not selected by default, this will be overwritten by the setCInterfaceItemColorsCurrentlyBeingUsed method if appropriate
        for(CInterfaceItem item : allColorsAvailItems)
            item.setSelection(false);

        //set all formats to be not selected by default, this will be overwritten by the setCInterfaceItemColorsCurrentlyBeingUsed method if appropriate
        for(CInterfaceItem item : allFormatsAvailItems)
            item.setSelection(false);
    }


        /*                                      */

    /*                                                                                          */ 
    public static CInterfaceItem setConfigMetaTitle(CInterfaceItem itemIn, String configPath, DataHandler dataHandler) {
        ItemStack item = itemIn.getItem();
        ItemMeta iMeta = item.getItemMeta();
        String[] title = FChat.newlineFormat(dataHandler.getYAMLStringField("config", "GUI_Buttons."+configPath));
        ArrayList<String> lore = new ArrayList<String>();
        for(int lineIndex = 0; lineIndex < title.length; lineIndex++) {
            if(lineIndex == 0)
                iMeta.setDisplayName(FChat.colFormat(title[0]));
            else
                lore.add(FChat.colFormat(title[lineIndex]));
        }
        iMeta.setLore(lore);
        item.setItemMeta(iMeta);
        itemIn.setItem(item);

        return(itemIn);
    }

    private static CInterfaceItem[] getAllUserItems() {
        //These are all items that serve a purpose(e.g Colors, Formats, buttons, this does not include filler and noPerm items)
        CInterfaceItem[] allItems = new CInterfaceItem[23];
        allItems[0] = preview;
        allItems[1] = applyChanges;
        allItems[2] = cancel;
        allItems[3] = reset;
        allItems[4] = blackColor;
        allItems[5] = dark_blueColor;
        allItems[6] = dark_greenColor;
        allItems[7] = cyanColor;
        allItems[8] = dark_redColor;
        allItems[9] = purpleColor;
        allItems[10] = orangeColor;
        allItems[11] = light_grayColor;
        allItems[12] = dark_grayColor;
        allItems[13] = light_blueColor;
        allItems[14] = light_greenColor;
        allItems[15] = aquaColor;
        allItems[16] = light_redColor;
        allItems[17] = pinkColor;
        allItems[18] = yellowColor;
        allItems[19] = magicFormat;
        allItems[20] = boldFormat;
        allItems[21] = italicFormat;
        allItems[22] = underlineFormat;

        return(allItems);
    }

    public static CInterfaceItem setMetaTitle(CInterfaceItem itemIn, String titleIn) {
        ItemStack item = itemIn.getItem();
        ItemMeta iMeta = item.getItemMeta();
        String[] title = FChat.newlineFormat(titleIn);
        ArrayList<String> lore = new ArrayList<String>();
        for(int lineIndex = 0; lineIndex < title.length; lineIndex++) {
            if(lineIndex == 0)
                iMeta.setDisplayName(FChat.colFormat(title[0]));
            else
                lore.add(FChat.colFormat(title[lineIndex]));
        }
        iMeta.setLore(lore);
        item.setItemMeta(iMeta);
        itemIn.setItem(item);
        return(itemIn);
    }

    public static CInterfaceItem addMetaEnchantGlow(CInterfaceItem itemIn) {
        ItemStack item = itemIn.getItem();
        ItemMeta iMeta = item.getItemMeta();
        iMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        iMeta.addEnchant(Enchantment.DAMAGE_ALL, 0, true);
        item.setItemMeta(iMeta);
        itemIn.setItem(item);
        return(itemIn); //NOTE: This is using the original itemIn value and changing that so using the return value wont be necessary in most cases
    }
    public static CInterfaceItem removeMetaEnchantGlow(CInterfaceItem itemIn) {
        ItemStack item = itemIn.getItem();
        ItemMeta iMeta = item.getItemMeta();
        iMeta.removeEnchant(Enchantment.DAMAGE_ALL);
        item.setItemMeta(iMeta);
        itemIn.setItem(item);
        return(itemIn); //NOTE: This is using the original itemIn value and changing that so using the return value wont be necessary in most cases
    }

    //Create a copy of an interface item
    public static CInterfaceItem cloneInterfaceItem(CInterfaceItem original) {
        CInterfaceItem instance = new CInterfaceItem(original); //calls the copy constructor
        return(instance);
    }

    //create a copy of an itnerface item while also changing the slot
    public static CInterfaceItem cloneInterfaceItem(CInterfaceItem original, int newSlot) {
        CInterfaceItem instance = new CInterfaceItem(original); //calls the copy constructor
        instance.setSlot(newSlot);
        return(instance);
    }

    //Convert a format sequence to the CInterfaceItem objects(arraylist) for the current colors that are being used(from format) also set their selection values to match this
    public static ArrayList<CInterfaceItem> setCInterfaceItemColorsCurrentlyBeingUsed(String formatSequence) {
        ArrayList<CInterfaceItem> out = new ArrayList<CInterfaceItem>();

        for(String sub : formatSequence.replaceAll("§", "").split("\\*")) {     //Note escape char for regex
            for(CInterfaceItem testItem : allColorsAvailItems) {
                if(testItem.getAssociatedColorCode().equals(sub)) {
                    testItem.setSelection(true);
                    out.add(testItem);
                }
            }
        }

        return(out);

    }

    //Get CInterfaceItem from slot
    public static CInterfaceItem getCInterfaceItem(int slot) {
        CInterfaceItem[] allItems = getAllUserItems();

        for(CInterfaceItem item : allItems) {
            if(item.getSlot() == slot) {
                return(item);
            }
        }
        return(null);
    }
}