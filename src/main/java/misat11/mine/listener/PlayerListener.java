package misat11.mine.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import misat11.mine.Main;
import misat11.mine.Mine;
import misat11.mine.MineBlock;
import misat11.mine.commands.MineCommand;

import static misat11.lib.lang.I18n.*;

public class PlayerListener implements Listener {

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}

		List<Mine> mines = Main.getMines();
		Player player = event.getPlayer();
		Location loc = event.getBlock().getLocation();
		for (Mine mine : mines) {
			if (isInArea(event.getBlock().getLocation(), mine.getPos1(), mine.getPos2())) {
				if (player.hasPermission(MineCommand.ADMIN_PERMISSION) && player.getGameMode() == GameMode.CREATIVE) {
					// ADMIN
					ItemStack stack = player.getInventory().getItemInMainHand();
					if (stack.getItemMeta().hasLore()) {
						List<String> lore = stack.getItemMeta().getLore();
						if (lore != null && lore.size() >= 1) {
							String action = lore.get(0);
							if (action.equals("misat11.mine.broker")) {
								List<MineBlock> blocks = mine.getMineBlocks();
								for (MineBlock block : new ArrayList<MineBlock>(blocks)) {
									if (block.getLocation().equals(loc)) {
										blocks.remove(block);
										player.sendMessage(i18n("block_removed_from_mine")
												.replace("%x%", Integer.toString(loc.getBlockX()))
												.replace("%y%", Integer.toString(loc.getBlockY()))
												.replace("%z%", Integer.toString(loc.getBlockZ())));
										break;
									}
								}
							}
						}
					}
				} else {
					// USER
					event.setCancelled(true);
					List<MineBlock> blocks = mine.getMineBlocks();
					for (MineBlock block : blocks) {
						if (block.getLocation().equals(loc)) {
							event.setCancelled(false);
							new BukkitRunnable() {

								@Override
								public void run() {
									block.spawnRandom(mine.getEntries(block.getEntrySet()), true);
								}

							}.runTaskLater(Main.getInstance(), block.getDuration() * 20L);
						}
					}
				}
				break;
			}
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}

		List<Mine> mines = Main.getMines();
		Player player = event.getPlayer();
		Location loc = event.getBlock().getLocation();
		for (Mine mine : mines) {
			if (isInArea(event.getBlock().getLocation(), mine.getPos1(), mine.getPos2())) {
				if (player.hasPermission(MineCommand.ADMIN_PERMISSION) && player.getGameMode() == GameMode.CREATIVE) {
					// ADMIN
					ItemStack stack = event.getItemInHand();
					if (stack.getItemMeta().hasLore()) {
						List<String> lore = stack.getItemMeta().getLore();
						if (lore != null && lore.size() >= 4) {
							String action = lore.get(0);
							String min = lore.get(1);
							String type = lore.get(2);
							int duration = Integer.parseInt(lore.get(3));
							if (action.equals("misat11.mine.build") && mine.getName().equals(min)) {
								List<MineBlock> blocks = mine.getMineBlocks();
								boolean founded = false;
								for (MineBlock block : new ArrayList<MineBlock>(blocks)) {
									if (block.getLocation().equals(loc)) {
										founded = true;
										player.sendMessage(i18n("block_already_exists")
												.replace("%x%", Integer.toString(loc.getBlockX()))
												.replace("%y%", Integer.toString(loc.getBlockY()))
												.replace("%z%", Integer.toString(loc.getBlockZ())));
										break;
									}
								}
								if (!founded) {
									MineBlock block = new MineBlock(loc, type, duration);
									player.sendMessage(i18n("block_added_to_mine")
											.replace("%x%", Integer.toString(loc.getBlockX()))
											.replace("%y%", Integer.toString(loc.getBlockY()))
											.replace("%z%", Integer.toString(loc.getBlockZ())));
									blocks.add(block);
									block.spawnRandom(mine.getEntries(type), true);
								}
							}
						}
					}
				} else {
					// USER
					event.setCancelled(true);
				}
				break;
			}
		}

	}

	public static boolean isInArea(Location l, Location p1, Location p2) {
		if (!p1.getWorld().equals(l.getWorld())) {
			return false;
		}

		Location min = new Location(p1.getWorld(), Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
				Math.min(p1.getZ(), p2.getZ()));
		Location max = new Location(p1.getWorld(), Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()),
				Math.max(p1.getZ(), p2.getZ()));
		return (min.getX() <= l.getX() && min.getY() <= l.getY() && min.getZ() <= l.getZ() && max.getX() >= l.getX()
				&& max.getY() >= l.getY() && max.getZ() >= l.getZ());
	}
}
