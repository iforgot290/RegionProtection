package me.neildennis.protection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.neildennis.protection.commands.ClaimCommand;
import me.neildennis.protection.listeners.ChatListener;
import me.neildennis.protection.listeners.InventoryListener;
import me.neildennis.protection.listeners.ProtListener;
import me.neildennis.protection.utils.Config;
import me.neildennis.protection.utils.Log;
import net.milkbowl.vault.economy.Economy;

public class Protection extends JavaPlugin{

	private static World mainworld;
	private static Protection instance;
	private static Economy economy;
	private static RegionManager manager;
	private static File regiondir;
	private static String regionpath;

	private static String green = ChatColor.DARK_GRAY + ">" + ChatColor.GREEN + ">" + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY;
	private static String red = ChatColor.DARK_GRAY + ">" + ChatColor.RED + ">" + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY;

	private static ArrayList<Region> regions;

	public void onEnable(){
		instance = this;
		Config.load();
		mainworld = Bukkit.getWorld(Config.getWorldName());
		manager = WGBukkit.getRegionManager(mainworld);
		regiondir = new File(this.getDataFolder() + "/regions/");
		regiondir.mkdirs();
		regionpath = regiondir.getAbsolutePath() + "/";
		loadRegions();

		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new EffectTask(), 40L, 40L);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> saveRegions(), 2400L, 2400L);
		this.getServer().getPluginManager().registerEvents(new ProtListener(), this);
		this.getServer().getPluginManager().registerEvents(new ChatListener(), this);
		this.getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		this.getCommand("prot").setExecutor(new ClaimCommand());
	}

	public void onDisable(){
		saveRegions();
	}
	
	public static void saveRegions(){
		int saved = 0;
		
		for (Region region : regions){
			File file = new File(regionpath + region.getRegion().getId() + ".yml");
			try {
				region.save(file);
				saved++;
			} catch (IOException e) {
				Log.severe("Error saving file: " + file.getName());
				e.printStackTrace();
			}
		}
		
		Log.info("Saved " + saved + " regions to file");
	}

	private static void loadRegions(){
		regions = new ArrayList<Region>();
		RegionManager manager = WGBukkit.getRegionManager(mainworld);
		for (File file : regiondir.listFiles()){
			if (file.getName().endsWith(".yml")){
				YamlConfiguration conf = new YamlConfiguration();
				try {
					conf.load(file);
					regions.add(new Region(conf, manager.getRegion(file.getName().replaceAll(".yml", ""))));
				} catch (IOException | InvalidConfigurationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Protection getPlugin(){
		return instance;
	}

	public static World getMainWorld(){
		return mainworld;
	}

	public static RegionManager getRegionManager(){
		return manager;
	}

	public static Economy getEco(){
		return economy;
	}

	public static void openMenu(Player pl){
		Inventory inv = ProtMenu.getProtMenu(pl);

		if (inv == null){
			pl.sendMessage(ChatColor.RED + "You must first claim land by crouching and placing a chest in the center of where you would like to claim");
			return;
		}
		
		pl.openInventory(inv);
	}

	public static Region getRegion(Player pl){
		return getRegion("prot_" + pl.getUniqueId());
	}

	public static Region getRegion(String id){
		for (Region region : regions)
			if (region.getRegion().getId().equalsIgnoreCase(id))
				return region;
		return null;
	}

	public static void removeRegion(Region region){
		manager.removeRegion(region.getRegion().getId());

		File file = new File(regiondir + region.getRegion().getId() + ".yml");
		file.delete();

		regions.remove(region);
	}

	public static boolean isIntersected(BlockVector loc, int radius, boolean allow){
		//EditSession es = WorldEdit.getInstance().getEditSessionFactory().getEditSession((com.sk89q.worldedit.world.World) new BukkitWorld(mainworld), 9999999);
		BlockVector minpoint = new BlockVector(loc.getBlockX() - radius, loc.getBlockY() - 20, loc.getBlockZ() - radius);
		BlockVector maxpoint = new BlockVector(loc.getBlockX() + radius, 256, loc.getBlockZ() + radius);
		ProtectedCuboidRegion region = new ProtectedCuboidRegion("test", minpoint, maxpoint);

		int check = allow == true ? 1 : 0;

		return region.getIntersectingRegions(manager.getRegions().values()).size() > check;
	}

	public static BlockVector getCenter(ProtectedRegion region){
		int xlen = region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX();
		int xmid = (xlen / 2) + region.getMinimumPoint().getBlockX();

		int zlen = region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ();
		int zmid = (zlen / 2) + region.getMinimumPoint().getBlockZ();

		int ymid = region.getMinimumPoint().getBlockY() + 20;

		return new BlockVector(xmid, ymid, zmid);
	}

	public static void expandRegion(ProtectedCuboidRegion region, BlockVector center, int radius) throws StorageException{
		manager.removeRegion(region.getId());
		BlockVector min = new BlockVector(center.getBlockX() - radius, center.getBlockY() - 20, center.getBlockZ() - radius);
		BlockVector max = new BlockVector(center.getBlockX() + radius, 256, center.getBlockZ() + radius);

		region.setMinimumPoint(min);
		region.setMaximumPoint(max);
		manager.addRegion(region);
	}

	public static void claimRegion(Player pl, BlockVector loc){

		int protRadius = Config.getInitialRadius();
		
		BlockVector minpoint = new BlockVector(loc.getBlockX() - protRadius, loc.getBlockY() - 20, loc.getBlockZ() - protRadius);
		BlockVector maxpoint = new BlockVector(loc.getBlockX() + protRadius, 256, loc.getBlockZ() + protRadius);
		ProtectedCuboidRegion region = new ProtectedCuboidRegion("prot_" + pl.getUniqueId().toString(), minpoint, maxpoint);

		DefaultDomain dom = new DefaultDomain();
		dom.addPlayer(pl.getUniqueId());

		region.setOwners(dom);
		manager.addRegion(region);

		try {
			region.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(WGBukkit.getPlugin(), Bukkit.getConsoleSender(), 
					green + "Now entering " + pl.getName() + "'s region"));
			region.setFlag(DefaultFlag.FAREWELL_MESSAGE, DefaultFlag.FAREWELL_MESSAGE.parseInput(WGBukkit.getPlugin(), Bukkit.getConsoleSender(), 
					red + "Now exiting " + pl.getName() + "'s region"));
		} catch (InvalidFlagFormat e1) {
			e1.printStackTrace();
		}

		YamlConfiguration conf = new YamlConfiguration();
		conf.set("owner", pl.getUniqueId().toString());

		try {
			conf.save(new File(regionpath + region.getId() + ".yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		regions.add(new Region(conf, region));
	}

}
