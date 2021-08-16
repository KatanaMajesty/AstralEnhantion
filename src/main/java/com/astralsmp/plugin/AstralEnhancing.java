package com.astralsmp.plugin;

import com.astralsmp.plugin.data.BlockEventHandler;
import com.astralsmp.plugin.data.ItemEventHandler;
import com.astralsmp.plugin.data.block.AventurineOre;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class AstralEnhancing extends JavaPlugin {

    public static Logger logger = Bukkit.getLogger();
    private static final AstralEnhancing instance = (AstralEnhancing) Bukkit.getPluginManager().getPlugin("AstralEnhancing");

    @Override
    public void onEnable() {
        new AventurineOre(this);

        getServer().getPluginManager().registerEvents(new BlockEventHandler(), this);
        getServer().getPluginManager().registerEvents(new ItemEventHandler(), this);

        logger.info("Плагин включён");
    }

    @Override
    public void onDisable() {
        logger.info("Плагин выключен");
    }

    public static AstralEnhancing getInstance() {
        return instance;
    }
}
