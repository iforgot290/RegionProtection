package me.neildennis.protection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

import me.neildennis.protection.utils.Config;

public class ProtMenu {

	public static Inventory getProtMenu(Player pl){
		Region region = Protection.getRegion("prot_" + pl.getUniqueId().toString());
		
		if (region == null) return null;

		Inventory inv = Bukkit.createInventory(null, 9, pl.getName() + "'s Region");

		inv.setItem(0, getSkull(pl, region));
		inv.setItem(1, getPVP(pl, region));
		inv.setItem(2, getFeather(pl, region));
		inv.setItem(3, getJump(pl, region));
		inv.setItem(7, getUpgradeItem(pl, region));
		inv.setItem(8, getRemoveItem());
		return inv;
	}

	public static ItemStack getSkull(Player pl, Region region){
		DefaultDomain members = region.getRegion().getMembers();
		ItemStack skullItem = new ItemStack(Material.SKULL_ITEM);

		ArrayList<String> lore = new ArrayList<String>();

		for (String name : members.getPlayers())
			lore.add(ChatColor.GRAY + name);

		for (UUID id : members.getUniqueIds())
			lore.add(ChatColor.GRAY + Bukkit.getOfflinePlayer(id).getName());

		if (lore.size() == 0)
			lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "Nobody");
		
		lore.add("");
		lore.add(ChatColor.GRAY + "Left click to add builders");
		lore.add(ChatColor.GRAY + "Right click to remove builders");
		lore.add(ChatColor.GRAY + "Shift-right click to clear builder list");

		SkullMeta meta = (SkullMeta) skullItem.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.setLore(lore);
		meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Builders");
		skullItem.setItemMeta(meta);
		
		return skullItem;
	}
	
	public static ItemStack getPVP(Player pl, Region region){
		ItemStack item = new ItemStack(Material.IRON_SWORD);
		ItemMeta meta = item.getItemMeta();
		
		if (region.getRegion().getFlag(DefaultFlag.PVP) == State.ALLOW)
			meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "PvP ON");
		else
			meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "PvP OFF");
		
		meta.setLore(Arrays.asList(new String[]{ChatColor.GRAY + "Click to toggle PVP in your region"}));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack getFeather(Player pl, Region region){
		ItemStack item = new ItemStack(Material.FEATHER);
		ItemMeta meta = item.getItemMeta();
		
		ArrayList<String> lore = new ArrayList<String>();
		
		if (region.hasEffect(PotionEffectType.SPEED)){
			if (region.isEffectActive(PotionEffectType.SPEED)){
				meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Speed Boost ON");
				meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
			} else {
				meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Speed Boost OFF");
			}
				
			lore.add(ChatColor.GRAY + "Click to toggle Speed Boost in your region");
		}
		
		else {
			meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Speed Boost");
			lore.add(ChatColor.GRAY + "Click to purchase");
			lore.add(ChatColor.GRAY + "Price: " + ChatColor.RED + Config.getEffectCost(PotionEffectType.SPEED));
		}
		
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack getJump(Player pl, Region region){
		ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
		ItemMeta meta = item.getItemMeta();

		ArrayList<String> lore = new ArrayList<String>();
		
		if (region.hasEffect(PotionEffectType.JUMP)){
			if (region.isEffectActive(PotionEffectType.JUMP)){
				meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Jump Boost ON");
				meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
			} else {
				meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Jump Boost OFF");
			}
				
			lore.add(ChatColor.GRAY + "Click to toggle Jump Boost in your region");
		}
		
		else {
			meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Jump Boost");
			lore.add(ChatColor.GRAY + "Click to purchase");
			lore.add(ChatColor.GRAY + "Price: " + ChatColor.RED + Config.getEffectCost(PotionEffectType.JUMP));
		}
		
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack getUpgradeItem(Player pl, Region region){
		ItemStack item = new ItemStack(Material.GRASS);
		ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Upgrade Region Size");
		
		int size = region.getRegion().getMaximumPoint().getBlockX() - region.getRegion().getMinimumPoint().getBlockX();
		ArrayList<String> lore = new ArrayList<String>();
		
		lore.add(ChatColor.RED + "Current Size: " + ChatColor.GRAY + size + "x" + size);
		
		int[] upgrade = Config.getNextSizeUpgrade(size / 2);
		
		if (upgrade[0] > 0){
			int upgradeSize = upgrade[0];
			int price = upgrade[1];
			lore.add(ChatColor.RED + "Next upgrade: " + ChatColor.GRAY + upgradeSize * 2 + "x" + upgradeSize * 2);
			lore.add(ChatColor.RED + "Price: " + ChatColor.GRAY + "$" + price);
		} else {
			lore.add(ChatColor.GRAY + "Fully upgraded!");
		}
		
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack getRemoveItem(){
		ItemStack item = new ItemStack(Material.REDSTONE);
		ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Remove Protection");
		meta.setLore(Arrays.asList(new String[]{ChatColor.GRAY + "Click to remove your currently claimed region", "",
				ChatColor.RED + "" + ChatColor.BOLD + "WARNING " + ChatColor.GRAY + "This is irreversable! No refunds!"}));
		
		item.setItemMeta(meta);
		return item;
	}

}
