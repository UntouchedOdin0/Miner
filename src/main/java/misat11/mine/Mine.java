package misat11.mine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Mine {

	private String name;
	private Location pos1;
	private Location pos2;
	private final Map<String, List<MineBlockEntry>> entries = new HashMap<String, List<MineBlockEntry>>();
	private final List<MineBlock> blocks = new ArrayList<MineBlock>();

	public Mine(String name, Location pos1, Location pos2) {
		this.name = name;
		this.pos1 = pos1;
		this.pos2 = pos2;
	}

	public static Mine load(File file) {
		if (!file.exists()) {
			return null;
		}
		FileConfiguration configMap = new YamlConfiguration();
		try {
			configMap.load(file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		String name = configMap.getString("name");
		Location pos1 = configMap.getSerializable("pos1", Location.class);
		Location pos2 = configMap.getSerializable("pos2", Location.class);
		Mine mine = new Mine(name, pos1, pos2);
		List<Map<String, Object>> listOfEntries = (List<Map<String, Object>>) configMap.getList("entries");
		for (Map<String, Object> entry : listOfEntries) {
			mine.entries.put((String) entry.get("name"), new ArrayList<MineBlockEntry>());
			List<MineBlockEntry> list = mine.entries.get((String) entry.get("name"));
			for (String ent : (List<String>) entry.get("types")) {
				String[] e = ent.split(":");
				Material material = Material.valueOf(e[0]);
				int chance = 1;
				int damage = 0;
				if (e.length >= 2) {
					chance = Integer.parseInt(e[1]);
					if (e.length >= 3) {
						damage = Integer.parseInt(e[2]);
					}
				}
				list.add(new MineBlockEntry(material, chance, damage));
			}
		}
		List<Map<String, Object>> blockList = (List<Map<String, Object>>) configMap.getList("blocks");
		for (Map<String, Object> block : blockList) {
			Location loc = (Location) block.get("location");
			String entrySet = (String) block.get("entrySet");
			int duration = (int) block.get("duration");
			MineBlock mineBlock = new MineBlock(loc, entrySet, duration);
			mine.blocks.add(mineBlock);
		}
		Main.addMine(mine);
		for (MineBlock block : mine.blocks) {
			try {
				block.spawnRandom(mine.getEntries(block.getEntrySet()), false);
			} catch (Throwable t) {
				
			}
		}
		return mine;
	}

	public void saveToConfig() {
		File dir = new File(Main.getInstance().getDataFolder(), "mines");
		if (!dir.exists())
			dir.mkdirs();
		File file = new File(dir, name + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileConfiguration configMap = new YamlConfiguration();
		configMap.set("name", name);
		configMap.set("pos1", pos1);
		configMap.set("pos2", pos2);
		List<Map<String, Object>> entryList = new ArrayList<Map<String, Object>>();
		for (Map.Entry<String, List<MineBlockEntry>> entry : entries.entrySet()) {
			Map<String, Object> localMap = new HashMap<String, Object>();
			List<String> stringList = new ArrayList<String>();
			for (MineBlockEntry en : entry.getValue()) {
				stringList.add(en.getMaterial().name() + ":" + en.getChance() + ":" + en.getDamage());
			}
			localMap.put("types", stringList);
			localMap.put("name", entry.getKey());
			entryList.add(localMap);
		}
		configMap.set("entries", entryList);
		List<Map<String, Object>> blockList = new ArrayList<Map<String, Object>>();
		for (MineBlock block : blocks) {
			Map<String, Object> localMap = new HashMap<String, Object>();
			localMap.put("location", block.getLocation());
			localMap.put("entrySet", block.getEntrySet());
			localMap.put("duration", block.getDuration());
			blockList.add(localMap);
		}
		configMap.set("blocks", blockList);
		try {
			configMap.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		return name;
	}

	public Location getPos1() {
		return pos1;
	}

	public Location getPos2() {
		return pos2;
	}
	
	public List<MineBlockEntry> getEntries(String name){
		return this.entries.get(name);
	}
	
	public List<String> getTypes() {
		return new ArrayList<String>(this.entries.keySet());
	}
	
	public boolean containsEntries(String name) {
		return this.entries.containsKey(name);
	}
	
	public void registerType(String name) {
		this.entries.put(name, new ArrayList<MineBlockEntry>());
	}
	
	public void unregisterType(String name) {
		this.entries.remove(name);
	}
	
	public List<MineBlock> getMineBlocks(){
		return this.blocks;
	}
	
}
