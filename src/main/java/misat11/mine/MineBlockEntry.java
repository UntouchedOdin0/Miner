package misat11.mine;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class MineBlockEntry {
	private Material material;
	private int damage = 0;
	private int chance = 1;
	
	public MineBlockEntry(Material material) {
		this.material = material;
	}
	
	public MineBlockEntry(Material material, int chance) {
		this.material = material;
		this.chance = chance;
	}
	
	public MineBlockEntry(Material material, int chance, int damage) {
		this.material = material;
		this.chance = chance;
		this.damage = damage;
	}

	public Material getMaterial() {
		return material;
	}

	public int getDamage() {
		return damage;
	}

	public int getChance() {
		return chance;
	}
	
	public void place(Location location) {
		Chunk chunk = location.getChunk();
		if (!chunk.isLoaded()) {
			chunk.load();
		}
		Block block = location.getBlock();
		block.setType(material);
		if (damage != 0) {
			try {
				// The method is no longer in API, but in legacy versions exists
				Block.class.getMethod("setData", byte.class).invoke(block, damage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
