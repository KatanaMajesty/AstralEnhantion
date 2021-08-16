package com.astralsmp.plugin.data.item;

import com.astralsmp.plugin.data.CustomItem;
import org.bukkit.plugin.Plugin;

public class ItAventurineOre extends CustomItem {

    public ItAventurineOre(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void init() {
        setItemName("Авантюриновый кристалл");
        setCustomModelDataID(9500);
        setNmsName("aventurine");
        setPlaceable(false);
        setLore(null);
    }
}

