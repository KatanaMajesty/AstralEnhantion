package com.astralsmp.plugin;

import com.astralsmp.plugin.api.PacketListener;
import com.astralsmp.plugin.data.BlockEventHandler;
import com.astralsmp.plugin.data.ItemEventHandler;
import com.astralsmp.plugin.data.block.AventurineOre;
import com.astralsmp.plugin.data.block.ChiseledEndStoneBricks;
import com.astralsmp.plugin.data.block.EndSoil;
import com.astralsmp.plugin.data.block.EndStem;
import com.astralsmp.plugin.modules.BlockRelated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

// TODO: 18.08.2021 топор на эффективность 5 ломает блоки слишком быстро и поэтому они ломаются как обычные нотные (вызывая баги)
// TODO: 17.08.2021 удаление кактуса, если при установке такой присутствует 
public class AstralEnhancing extends JavaPlugin {

    public static Logger logger = Bukkit.getLogger();
    private static final AstralEnhancing instance = (AstralEnhancing) Bukkit.getPluginManager().getPlugin("AstralEnhancing");

    @Override
    public void onEnable() {
        BlockRelated.initReplaceableArray();
        BlockRelated.initWoodenArray();

        new AventurineOre(this);
        new ChiseledEndStoneBricks(this);
        new EndSoil(this);
        new EndStem(this);

        getServer().getPluginManager().registerEvents(new BlockEventHandler(this), this);
        getServer().getPluginManager().registerEvents(new ItemEventHandler(this), this);

        logger.info("Плагин включён");
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            PacketListener.removePlayer(p);
        }
        logger.info("Плагин выключен");
    }

    public static AstralEnhancing getInstance() {
        return instance;
    }
}
