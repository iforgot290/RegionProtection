package me.neildennis.protection.utils;

import org.bukkit.Bukkit;

public class Log {
	
	public static void debug(Object obj){
		Bukkit.broadcastMessage(obj.toString());
	}
	
	public static void info(Object obj){
		Bukkit.getLogger().info(obj.toString());
	}
	
	public static void warn(Object obj){
		Bukkit.getLogger().warning(obj.toString());
	}
	
	public static void severe(Object obj){
		Bukkit.getLogger().severe(obj.toString());
	}

}
