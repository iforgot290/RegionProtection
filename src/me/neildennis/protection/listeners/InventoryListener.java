package me.neildennis.protection.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

import me.neildennis.protection.ProtMenu;
import me.neildennis.protection.Protection;
import me.neildennis.protection.Region;
import me.neildennis.protection.listeners.ChatListener.ChatState;
import me.neildennis.protection.utils.Config;
import net.milkbowl.vault.economy.Economy;

public class InventoryListener implements Listener{

	public InventoryListener(){

	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event){
		if (!event.getInventory().getName().endsWith("'s Region")) return;
		event.setResult(Result.DENY);

		if (event.getWhoClicked().getType() != EntityType.PLAYER) return;
		if (event.getCurrentItem() == null) return;

		Player pl = (Player) event.getWhoClicked();
		Region region = Protection.getRegion("prot_" + event.getWhoClicked().getUniqueId());

		switch (event.getCurrentItem().getType()){

		case REDSTONE:
			event.getWhoClicked().closeInventory();
			pl.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "WARNING " + ChatColor.GRAY + "You are about to delete your claimed region. "
					+ "Please type \"confirm\" to continue.");
			ChatListener.addPlayer(pl, ChatState.DELETE);
			break;

		case IRON_SWORD:
			boolean pvp = region.getRegion().getFlag(DefaultFlag.PVP) == State.ALLOW ? true : false;
			region.getRegion().setFlag(DefaultFlag.PVP, pvp == true ? State.DENY : State.ALLOW);
			event.setCurrentItem(ProtMenu.getPVP(pl, region));
			break;

		case FEATHER:
			if (region.hasEffect(PotionEffectType.SPEED)){
				region.toggleEffect(PotionEffectType.SPEED);
			} else {
				Economy eco = Protection.getEco();

				double cash = eco.getBalance(pl);
				double cost = Config.getEffectCost(PotionEffectType.SPEED);

				if (cost > cash){
					pl.sendMessage(ChatColor.RED + "Insufficient funds");
					break;
				}

				eco.withdrawPlayer(pl, cost);
				region.addEffect(PotionEffectType.SPEED);
			}

			event.setCurrentItem(ProtMenu.getFeather(pl, region));
			break;
			
		case LEATHER_BOOTS:
			if (region.hasEffect(PotionEffectType.JUMP)){
				region.toggleEffect(PotionEffectType.JUMP);
			} else {
				Economy eco = Protection.getEco();
				
				double cash = eco.getBalance(pl);
				double cost = Config.getEffectCost(PotionEffectType.JUMP);
				
				if (cost > cash){
					pl.sendMessage(ChatColor.RED + "Insufficient funds");
					break;
				}
				
				eco.withdrawPlayer(pl, cost);
				region.addEffect(PotionEffectType.JUMP);
			}
			
			event.setCurrentItem(ProtMenu.getJump(pl, region));
			break;

		case GRASS:
			int size = region.getRegion().getMaximumPoint().getBlockX() - region.getRegion().getMinimumPoint().getBlockX();
			int[] upgrade = Config.getNextSizeUpgrade(size / 2);

			if (upgrade[0] == 0) break;

			BlockVector center = Protection.getCenter(region.getRegion());
			int upgradeSize = upgrade[0];

			if (Protection.isIntersected(center, upgradeSize, true)){
				pl.sendMessage(ChatColor.RED + "Error: Another region is in the way");
				pl.closeInventory();
				break;
			}

			Economy eco = Protection.getEco();
			int cost = upgrade[1];
			int cash = (int) eco.getBalance(pl);

			if (cost > cash){
				pl.sendMessage(ChatColor.RED + "Insufficient funds");
				pl.closeInventory();
				break;
			}

			try {
				Protection.expandRegion((ProtectedCuboidRegion) region.getRegion(), center, upgradeSize);
				eco.withdrawPlayer(pl, cost);
				pl.sendMessage(ChatColor.GREEN + "Expanded!");
			} catch (StorageException e) {
				e.printStackTrace();
				pl.sendMessage(ChatColor.RED + "Error: Please contact administrator");
				pl.closeInventory();
				break;
			}

			event.setCurrentItem(ProtMenu.getUpgradeItem(pl, region));
			break;

		case SKULL_ITEM:
			if (event.isLeftClick()){
				ChatListener.addPlayer(pl, ChatState.ADD_MEMBER);
				pl.closeInventory();
				pl.sendMessage(ChatColor.GREEN + "Type any player's name to add them to your build list");
				break;
			} else {
				if (event.isShiftClick()){
					DefaultDomain domain = region.getRegion().getMembers();
					domain.removeAll();
					region.getRegion().setMembers(domain);
					pl.sendMessage(ChatColor.RED + "Build list successfully cleared");
				} else {
					ChatListener.addPlayer(pl, ChatState.REMOVE_MEMBER);
					pl.closeInventory();
					pl.sendMessage(ChatColor.GREEN + "Type any player's name to remove them from your build list");
					break;
				}
			}
			
			event.setCurrentItem(ProtMenu.getSkull(pl, region));
			break;

		default:
			break;

		}
	}

}
