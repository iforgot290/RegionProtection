package me.neildennis.protection;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class EffectTask implements Runnable{
	
	private RegionManager manager;
	
	public EffectTask(){
		manager = Protection.getRegionManager();
	}
	
	@Override
	public void run(){
		
		for (Player pl : Bukkit.getOnlinePlayers()){
			for (ProtectedRegion protregion : manager.getApplicableRegions(pl.getLocation())){
				if (protregion.getId().startsWith("prot_")){
					Region region = Protection.getRegion(protregion.getId());
					for (Entry<PotionEffectType, Boolean> effect : region.getEffects().entrySet()){
						if (effect.getValue() == true){
							pl.removePotionEffect(effect.getKey());
							pl.addPotionEffect(new PotionEffect(effect.getKey(), 100, 0));
						}
					}
				}
			}
		}
		
	}

}
