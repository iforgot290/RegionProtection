package me.neildennis.protection;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Region {
	
	private ProtectedRegion region;
	private HashMap<PotionEffectType, Boolean> effects;
	private YamlConfiguration config;
	
	public Region(YamlConfiguration config, ProtectedRegion region){
		this.effects = new HashMap<PotionEffectType, Boolean>();
		this.region = region;
		this.config = config;
		
		if (config.getConfigurationSection("effects") == null) return;
		
		for (Entry<String, Object> ent : config.getConfigurationSection("effects").getValues(true).entrySet()){
			effects.put(PotionEffectType.getByName(ent.getKey()), (Boolean) ent.getValue());
		}
	}
	
	public ProtectedRegion getRegion(){
		return region;
	}
	
	public boolean hasEffect(PotionEffectType type){
		return effects.containsKey(type);
	}
	
	public boolean isEffectActive(PotionEffectType type){
		return effects.get(type);
	}
	
	public void toggleEffect(PotionEffectType type){
		effects.put(type, !effects.get(type));
	}
	
	public void addEffect(PotionEffectType type){
		effects.put(type, true);
		config.set("effects." + type.getName(), true);
	}
	
	public HashMap<PotionEffectType, Boolean> getEffects(){
		return effects;
	}
	
	public void save(File file) throws IOException{
		config.save(file);
	}

}
