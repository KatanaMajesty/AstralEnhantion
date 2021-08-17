package com.astralsmp.plugin.data.item;

import com.astralsmp.plugin.data.CustomItem;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.plugin.Plugin;

public class ItChiseledEndStoneBricks extends CustomItem {

    public ItChiseledEndStoneBricks(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void init() {
        setInstrument(Instrument.BELL);
        setNote(new Note(3));
        setPlaceable(true);
        setPlaceSound("block.stone.place");
        setItemName("Резные эндерняковые кирпичи");
        setCustomModelDataID(9622);
        setNmsName("chiseled_end_stone_bricks");
        setLore(null);
    }
}
