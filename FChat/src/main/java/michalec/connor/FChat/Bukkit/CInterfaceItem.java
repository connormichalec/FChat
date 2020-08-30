package michalec.connor.FChat.Bukkit;

import org.bukkit.inventory.ItemStack;

public class CInterfaceItem {
    private ItemStack item;
    private int slot;
    private String associatedColorCode = null;

    private Boolean currentlySelected = null;

    //Should be used by all other items:
    public CInterfaceItem(ItemStack item, int slot) {
        this.item = item;
        this.slot = slot;
    }

    //Should just be used by colors:
    public CInterfaceItem(String associatedColorCode, ItemStack item, int slot) {
        this.item = item;
        this.slot = slot;
        this.associatedColorCode = associatedColorCode;
    }


    
    //Copy constructor, https://stackoverflow.com/questions/869033/how-do-i-copy-an-object-in-java, https://www.geeksforgeeks.org/copy-constructor-in-java/
    public CInterfaceItem(CInterfaceItem oldInstance) {
        this.item = oldInstance.item;
        this.slot = oldInstance.slot;
        this.currentlySelected = oldInstance.currentlySelected;
        this.associatedColorCode = oldInstance.associatedColorCode;
    }


    public Boolean isCurrentlySelected() {
        return (currentlySelected);
    }

    public ItemStack getItem() {
        return (item);
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public int getSlot() {
        return (slot);
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String getAssociatedColorCode() {
        return(associatedColorCode);
    }

    public void setSelection(Boolean val) {
        this.currentlySelected = val;
    }
}