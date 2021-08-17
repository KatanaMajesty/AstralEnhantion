package com.astralsmp.plugin.data.item;

import com.astralsmp.plugin.data.CustomItem;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.plugin.Plugin;

public class ItEndSoil extends CustomItem {

    public ItEndSoil(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void init() {
        setInstrument(Instrument.BELL);
        setNote(new Note(1));
        setPlaceable(true);
        setNmsName("end_soil");
        setItemName("Почва Края");
        setPlaceSound("block.dripstone_block.place");
        setLore(null);
        setCustomModelDataID(9620);
    }
}
