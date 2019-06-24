package misat11.mine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class MineBlock {
	private Location location;
	private String entrySet;
	private int duration;
	
	public MineBlock(Location location, String entrySet, int duration) {
		this.location = location;
		this.entrySet = entrySet;
		this.duration = duration;
	}
	
	public Location getLocation() {
		return this.location;
	}

	public String getEntrySet() {
		return this.entrySet;
	}
	
	public int getDuration() {
		return this.duration;
	}

	public void spawnRandom(List<MineBlockEntry> entries, boolean override) {
		if (entries == null) {
			return;
		}
		Chunk chunk = location.getChunk();
		if (!chunk.isLoaded()) {
			chunk.load();
		}
		Block block = location.getBlock();
		if (block.getType() == Material.AIR || override) {
			if (entries.size() == 1) {
				entries.get(0).place(location);
			} else if (entries.size() > 1) {
				Map<Integer, MineBlockEntry> map = new HashMap<Integer, MineBlockEntry>();
				for (MineBlockEntry entry : entries) {
					for (int i = 1; i <= entry.getChance(); i++) {
						map.put(map.size(), entry);
					}
				}
				Random rand = new Random();
				int pos = rand.nextInt(map.size());
				map.get(pos).place(location);
			}
		}
	}
}
