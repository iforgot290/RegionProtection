package me.neildennis.protection.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;

import me.neildennis.protection.Protection;

public class Config {
	
	private static HashMap<Integer, Integer> upgrades = new HashMap<Integer, Integer>();
	private static YamlConfiguration config;
	
	public static void load(){
		upgrades.put(25, 1000);
		config = new YamlConfiguration();
		
		Protection.getPlugin().getDataFolder().mkdirs();
		File conf = new File(Protection.getPlugin().getDataFolder() + "/config.yml");
		
		if (!conf.exists()){
			try {
				config.save(conf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			config.load(conf);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public static double getEffectCost(PotionEffectType type){
		return config.getDouble(type.getName());
	}
	
	public static double getClaimCost(){
		return config.getDouble("claim");
	}
	
	public static String getWorldName(){
		String name = config.getString("world");
		return name != null ? name : "world";
	}
	
	public static int[] getNextSizeUpgrade(int radius){
		
		int nextSize = 0;
		int price = 0;
		
		for (Entry<Integer, Integer> ent : upgrades.entrySet()){
			if (ent.getKey() > radius){
				nextSize = ent.getKey();
				price = ent.getValue();
			}
		}
		
		return new int[] { nextSize, price };
	}

	public static int getInitialRadius() {
		return config.getInt("initial");
	}

}
