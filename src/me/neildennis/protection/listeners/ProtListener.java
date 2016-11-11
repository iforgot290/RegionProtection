package me.neildennis.protection.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.sk89q.worldedit.BlockVector;

import me.neildennis.protection.Protection;
import me.neildennis.protection.utils.Config;
import net.milkbowl.vault.economy.Economy;

public class ProtListener implements Listener{

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		if (event.getBlock().getType().equals(Material.CHEST)){
			if (event.getBlock().getLocation().getWorld() != Protection.getMainWorld()) return;
			if (!event.getPlayer().isSneaking()) return;
			if (Protection.getRegion("prot_" + event.getPlayer().getUniqueId().toString()) != null) return;

			Location loc = event.getBlock().getLocation();
			BlockVector vector = new BlockVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			
			if (Protection.isIntersected(vector, 50, false)){
				event.getPlayer().sendMessage(ChatColor.RED + "This is too close to someone else's land!");
				event.setCancelled(true);
				return;
			}
			
			Economy eco = Protection.getEco();
			double cash = eco.getBalance(event.getPlayer());
			double cost = Config.getClaimCost();
			
			if (cash < cost){
				event.getPlayer().sendMessage(ChatColor.RED + "You do not have enough money to claim this land (Need $" + (int) (cost - cash) + " more)");
				event.setCancelled(true);
				return;
			}
			
			eco.withdrawPlayer(event.getPlayer(), cost);
			Protection.claimRegion(event.getPlayer(), vector);
			event.getPlayer().sendMessage(ChatColor.GREEN + "Your house is now protected! Use " + ChatColor.YELLOW + "/prot " + ChatColor.GREEN + 
					"to manage your claim."); 
		}
	}

}
