package com.astralsmp.plugin.data.item;

import com.astralsmp.plugin.data.CustomItem;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.plugin.Plugin;

public class ItEndStem extends CustomItem {

    public ItEndStem(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void init() {
        setNote(new Note(2));
        setInstrument(Instrument.BELL);
        setCustomModelDataID(9621);
        setItemName("Корень Края");
        setNmsName("poise_stem");
        setPlaceable(true);
        setPlaceSound("custom.block.wood.place");
    }
}
