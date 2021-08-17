package com.astralsmp.plugin.data;

import com.astralsmp.plugin.api.PacketAPI;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.List;

public abstract class CustomItem {

    private static PacketAPI api = new PacketAPI();

    static final String CLASS_ID = "astral_item";
    static final String CLASS_ID_B = "astral_placeable_item";
    private final Plugin plugin;
    private String itemName;
    private String nmsName;
    private boolean placeable = false;
    @Nullable
    private List<String> lore;
    private Integer customModelDataID;
    private String placeSound;
    private ItemStack item;
    private NBTTagCompound nbtTagCompound;
    private Instrument instrument;
    private Note note;

    private void closeItemCreation() {
        throw new NullPointerException(String.format("Field of ITEM is Null at %s", getClass().getName()));
    }

    public CustomItem(Plugin plugin) {
        this.plugin = plugin;
        init();
        if (itemName == null
                | nmsName == null
                | customModelDataID == null
                | (placeable && placeSound == null)) closeItemCreation();
        createItem();
        ItemEventHandler.customItems.add(this);
    }

    public abstract void init();

    private void createItem() {
        item = new ItemStack(Material.PHANTOM_MEMBRANE);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f" + itemName));
        meta.setCustomModelData(customModelDataID);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        NBTTagProcessing();
    }

    private void NBTTagProcessing() {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        nbtTagCompound = nmsItem.getTag() != null ? nmsItem.getTag() : new NBTTagCompound();
        nbtTagCompound.set(CLASS_ID, NBTTagString.a(nmsName));
        if (placeable) nbtTagCompound.set(CLASS_ID_B, NBTTagString.a("placeable"));
        nmsItem.setTag(nbtTagCompound);
        item = CraftItemStack.asBukkitCopy(nmsItem);
    }

    /*
    ====================
    ГЕТТЕРЫ
    ====================
     */

    public Plugin getPlugin() {
        return plugin;
    }

    public String getItemName() {
        return itemName;
    }

    public String getNmsName() {
        return nmsName;
    }

    public boolean isPlaceable() {
        return placeable;
    }

    @Nullable
    public List<String> getLore() {
        return lore;
    }

    public Integer getCustomModelDataID() {
        return customModelDataID;
    }

    public String getPlaceSound() {
        return placeSound;
    }

    public ItemStack getItem() {
        return item;
    }

    public NBTTagCompound getNbtTagCompound() {
        return nbtTagCompound;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public Note getNote() {
        return note;
    }

    /*
    ====================
    СЕТТЕРЫ
    ====================
     */

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setNmsName(String nmsName) {
        this.nmsName = nmsName;
    }

    public void setPlaceable(boolean placeable) {
        this.placeable = placeable;
    }

    public void setLore(@Nullable List<String> lore) {
        this.lore = lore;
    }

    public void setCustomModelDataID(Integer customModelDataID) {
        this.customModelDataID = customModelDataID;
    }

    public void setPlaceSound(String placeSound) {
        this.placeSound = placeSound;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public void setNbtTagCompound(NBTTagCompound nbtTagCompound) {
        this.nbtTagCompound = nbtTagCompound;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public void setNote(Note note) {
        this.note = note;
    }
}
