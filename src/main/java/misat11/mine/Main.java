package misat11.mine;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import misat11.lib.lang.I18n;
import misat11.mine.commands.MineCommand;
import misat11.mine.listener.PlayerListener;

public class Main extends JavaPlugin {
	private static Main instance;
	private String version;
	private boolean snapshot;
	private HashMap<String, Mine> mines = new HashMap<String, Mine>();
	
	public static Main getInstance() {
		return instance;
	}
	
	public static String getVersion() {
		return instance.version;
	}
	
	public static boolean isSnapshot() {
		return instance.snapshot;
	}
	
	public static void addMine(Mine mine) {
		instance.mines.put(mine.getName(), mine);
	}
	
	public static void removeMine(Mine mine) {
		instance.mines.remove(mine.getName());
	}
	
	public static boolean isMineExists(String name) {
		return instance.mines.containsKey(name);
	}
	
	public static Mine getMine(String name) {
		return instance.mines.get(name);
	}
	
	public static List<String> getMineNames() {
		return new ArrayList<String>(instance.mines.keySet());
	}
	
	public static List<Mine> getMines(){
		return new ArrayList<Mine>(instance.mines.values());
	}
	
	@Override
	public void onEnable() {
		instance = this;
		version = this.getDescription().getVersion();
		snapshot = version.toLowerCase().contains("pre");
		
		saveDefaultConfig();

		I18n.load(this, getConfig().getString("locale"));
		
		MineCommand cmd = new MineCommand();
		
		getCommand("mine").setExecutor(cmd);
		getCommand("mine").setTabCompleter(cmd);
		
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);

		Bukkit.getLogger().info("********************");
		Bukkit.getLogger().info("*       Miner      *");
		Bukkit.getLogger().info("*    by Misat11    *");
		Bukkit.getLogger().info("*                  *");
		if (version.length() == 10) {
			Bukkit.getLogger().info("*                  *");
			Bukkit.getLogger().info("*    V" + version + "   *");
		} else {
			Bukkit.getLogger().info("*      V" + version + "      *");
		}
		Bukkit.getLogger().info("*                  *");
		if (snapshot == true) {
			Bukkit.getLogger().info("* SNAPSHOT VERSION *");
		} else {
			Bukkit.getLogger().info("*  STABLE VERSION  *");
		}
		Bukkit.getLogger().info("*                  *");

		Bukkit.getLogger().info("*                  *");
		Bukkit.getLogger().info("********************");

		File folder = new File(getDataFolder().toString(), "mines");
		if (folder.exists()) {
			File[] listOfFiles = folder.listFiles();
			if (listOfFiles.length > 0) {
				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile()) {
						Mine.load(listOfFiles[i]);
					}
				}
			}
		}
	}
	
	@Override
	public void onDisable() {

	}
}
