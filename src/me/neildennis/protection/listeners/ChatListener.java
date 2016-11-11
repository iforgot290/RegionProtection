package me.neildennis.protection.listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.sk89q.worldguard.domains.DefaultDomain;

import me.neildennis.protection.Protection;
import me.neildennis.protection.Region;

public class ChatListener implements Listener{

	private static HashMap<Player, ChatState> players;

	public ChatListener(){
		players = new HashMap<Player, ChatState>();
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event){
		if (players.containsKey(event.getPlayer())){
			event.setCancelled(true);

			Player pl = event.getPlayer();
			String message = event.getMessage();
			ChatState state = players.get(pl);

			if (state == ChatState.ADD_MEMBER){
				if (message.contains(" ") || message.length() > 16){
					pl.sendMessage(ChatColor.RED + "Invalid player");
					return;
				}

				@SuppressWarnings("deprecation")
				OfflinePlayer toadd = Bukkit.getOfflinePlayer(message);

				Region region = Protection.getRegion(pl);
				DefaultDomain domain = region.getRegion().getMembers();
				domain.addPlayer(toadd.getUniqueId());
				region.getRegion().setMembers(domain);

				pl.sendMessage(ChatColor.GREEN + toadd.getName() + " added to build list");
				Protection.openMenu(pl);
				players.remove(pl);
			} else if (state == ChatState.REMOVE_MEMBER){
				if (message.contains(" ") || message.length() > 16){
					pl.sendMessage(ChatColor.RED + "Invalid player");
					return;
				}
				
				@SuppressWarnings("deprecation")
				OfflinePlayer torem = Bukkit.getOfflinePlayer(message);
				
				Region region = Protection.getRegion(pl);
				DefaultDomain domain = region.getRegion().getMembers();
				
				if (!domain.contains(torem.getUniqueId())){
					pl.sendMessage(ChatColor.RED + "Invalid player");
					return;
				}
				
				domain.removePlayer(torem.getUniqueId());
				region.getRegion().setMembers(domain);
				
				pl.sendMessage(ChatColor.RED + torem.getName() + " removed from build list");
				Protection.openMenu(pl);
				players.remove(pl);
			} else if (state == ChatState.DELETE){
				if (message.equalsIgnoreCase("confirm")){
					Protection.removeRegion(Protection.getRegion(pl));
					pl.sendMessage(ChatColor.RED + "You have unclaimed your current region");
				} else {
					pl.sendMessage(ChatColor.RED + "Deletion cancelled");
				}
				players.remove(pl);
			}
		}
	}

	public static void addPlayer(Player pl, ChatState state){
		players.put(pl, state);
	}

	public enum ChatState{
		ADD_MEMBER, REMOVE_MEMBER, DELETE;
	}

}
