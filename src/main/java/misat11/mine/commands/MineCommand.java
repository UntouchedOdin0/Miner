package misat11.mine.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import misat11.mine.Main;
import misat11.mine.Mine;
import misat11.mine.MineBlock;
import misat11.mine.MineBlockEntry;

import static misat11.lib.lang.I18n.*;

public class MineCommand implements CommandExecutor, TabCompleter {

	public static final String ADMIN_PERMISSION = "misat11.mine.admin";

	public final Map<Player, Selection> selections = new HashMap<Player, Selection>();

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completionList = new ArrayList<>();
		if (sender instanceof Player && sender.hasPermission(ADMIN_PERMISSION)) {
			if (args.length == 1) {
				List<String> cmds = Arrays.asList("pos1", "pos2", "add", "addtypelist", "addentry", "removeentry",
						"removetypelist", "build", "broker", "save", "remove", "reload", "regen");
				StringUtil.copyPartialMatches(args[0], cmds, completionList);
			}
			if (args.length == 2 && (args[0].equalsIgnoreCase("addtypelist") || args[0].equalsIgnoreCase("addentry")
					|| args[0].equalsIgnoreCase("removeentry") || args[0].equalsIgnoreCase("removetypelist")
					|| args[0].equalsIgnoreCase("build") || args[0].equalsIgnoreCase("save")
					|| args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("regen"))) {
				List<String> cmds = Main.getMineNames();
				StringUtil.copyPartialMatches(args[1], cmds, completionList);
			}
			if (args.length == 3 && (args[0].equalsIgnoreCase("addentry") || args[0].equalsIgnoreCase("removeentry")
					|| args[0].equalsIgnoreCase("removetypelist") || args[0].equalsIgnoreCase("build"))) {
				if (Main.isMineExists(args[1])) {
					Mine mine = Main.getMine(args[1]);
					List<String> cmds = mine.getTypes();
					StringUtil.copyPartialMatches(args[2], cmds, completionList);
				}

			}
			if (args.length == 4 && (args[0].equalsIgnoreCase("addentry") || args[0].equalsIgnoreCase("removeentry"))) {
				Material[] materials = Material.values();
				List<String> cmds = new ArrayList<String>();
				for (Material mat : materials) {
					if (mat.isBlock()) {
						cmds.add(mat.name());
					}
				}
				StringUtil.copyPartialMatches(args[3], cmds, completionList);
			}
			if (args.length == 4 && args[0].equalsIgnoreCase("build")) {
				StringUtil.copyPartialMatches(args[3], Arrays.asList("30", "60", "90", "120"), completionList);
			}
			if (args.length == 5 && args[0].equalsIgnoreCase("addentry")) {
				StringUtil.copyPartialMatches(args[4], Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9"),
						completionList);
			}
		}
		return completionList;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (sender.hasPermission(ADMIN_PERMISSION)) {
				if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
					sendHelp(player);
				} else if (args[0].equalsIgnoreCase("pos1")) {
					if (!selections.containsKey(player)) {
						selections.put(player, new Selection());
					}
					Selection selection = selections.get(player);
					selection.pos1 = player.getLocation();
					player.sendMessage(i18n("pos_selected").replace("%position%", "1")
							.replace("%x%", Integer.toString(selection.pos1.getBlockX()))
							.replace("%y%", Integer.toString(selection.pos1.getBlockY()))
							.replace("%z%", Integer.toString(selection.pos1.getBlockZ())));
				} else if (args[0].equalsIgnoreCase("pos2")) {
					if (!selections.containsKey(player)) {
						selections.put(player, new Selection());
					}
					Selection selection = selections.get(player);
					selection.pos2 = player.getLocation();
					player.sendMessage(i18n("pos_selected").replace("%position%", "2")
							.replace("%x%", Integer.toString(selection.pos2.getBlockX()))
							.replace("%y%", Integer.toString(selection.pos2.getBlockY()))
							.replace("%z%", Integer.toString(selection.pos2.getBlockZ())));
				} else if (args[0].equalsIgnoreCase("add") && args.length >= 2) {
					String name = args[1];
					if (Main.isMineExists(name)) {
						player.sendMessage(i18n("game_already_exists"));
					} else {
						Selection selection = selections.get(player);
						if (selection == null || selection.pos1 == null || selection.pos2 == null) {
							player.sendMessage(i18n("you_must_do_selection"));
						} else {
							Mine mine = new Mine(name, selection.pos1, selection.pos2);
							Main.addMine(mine);
							player.sendMessage(i18n("mine_created").replace("%name%", name));
						}
					}

				} else if (args[0].equalsIgnoreCase("addtypelist") && args.length >= 3) {
					String name = args[1];
					if (Main.isMineExists(name)) {
						Mine mine = Main.getMine(name);
						String type = args[2];
						if (mine.containsEntries(type)) {
							player.sendMessage(i18n("type_already_exists"));
						} else {
							mine.registerType(type);
							player.sendMessage(i18n("type_created").replace("%name%", name).replace("%type%", type));
						}
					} else {
						player.sendMessage(i18n("mine_is_not_exist"));
					}

				} else if (args[0].equalsIgnoreCase("addentry") && args.length >= 4) {
					String name = args[1];
					if (Main.isMineExists(name)) {
						Mine mine = Main.getMine(name);
						String type = args[2];
						if (mine.containsEntries(type)) {
							String material = args[3].toUpperCase();
							int chance = 1;
							int damage = 0;
							if (args.length >= 5) {
								chance = Integer.parseInt(args[4]);
								if (args.length >= 6) {
									damage = Integer.parseInt(args[5]);
								}
							}
							Material mat = Material.getMaterial(material);
							if (mat == null) {
								player.sendMessage(i18n("unknown_material"));
							} else {
								MineBlockEntry entry = new MineBlockEntry(mat, chance, damage);
								mine.getEntries(type).add(entry);
								player.sendMessage(
										i18n("entry_created").replace("%material%", material).replace("%type%", type));
							}
						} else {
							player.sendMessage(i18n("type_is_not_exist"));
						}

					} else {
						player.sendMessage(i18n("mine_is_not_exist"));
					}

				} else if (args[0].equalsIgnoreCase("removeentry") && args.length >= 4) {
					String name = args[1];
					if (Main.isMineExists(name)) {
						Mine mine = Main.getMine(name);
						String type = args[2];
						if (mine.containsEntries(type)) {
							String material = args[3].toUpperCase();
							Material mat = Material.getMaterial(material);
							if (mat == null) {
								player.sendMessage(i18n("unknown_material"));
							} else {
								List<MineBlockEntry> entries = mine.getEntries(type);
								for (MineBlockEntry entry : new ArrayList<MineBlockEntry>(entries)) {
									if (entry.getMaterial() == mat) {
										entries.remove(entry);
									}
								}
								player.sendMessage(i18n("entry_was_removed").replace("%material%", material)
										.replace("%type%", type));
							}
						} else {
							player.sendMessage(i18n("type_is_not_exist"));
						}
					} else {
						player.sendMessage(i18n("mine_is_not_exist"));
					}

				} else if (args[0].equalsIgnoreCase("removetypelist") && args.length >= 3) {
					String name = args[1];
					if (Main.isMineExists(name)) {
						Mine mine = Main.getMine(name);
						String type = args[2];
						if (mine.containsEntries(type)) {
							mine.unregisterType(type);
							player.sendMessage(
									i18n("type_was_removed").replace("%name%", name).replace("%type%", type));
						} else {
							player.sendMessage(i18n("type_is_not_exist"));
						}

					} else {
						player.sendMessage(i18n("mine_is_not_exist"));
					}

				} else if (args[0].equalsIgnoreCase("build") && args.length >= 4) {
					String name = args[1];
					if (Main.isMineExists(name)) {
						Mine mine = Main.getMine(name);
						String type = args[2];
						String duration = args[3];
						if (mine.containsEntries(type)) {
							ItemStack stack = new ItemStack(Material.STONE);
							ItemMeta meta = stack.getItemMeta();
							meta.setDisplayName(name + " - " + type + " (" + duration + " s)");
							List<String> lore = new ArrayList<String>();
							lore.add("misat11.mine.build");
							lore.add(name);
							lore.add(type);
							lore.add(duration);
							meta.setLore(lore);
							stack.setItemMeta(meta);
							player.getInventory().addItem(stack);
							player.sendMessage(
									i18n("you_got_block").replace("%type%", type).replace("%duration%", duration));
						} else {
							player.sendMessage(i18n("type_is_not_exist"));
						}

					} else {
						player.sendMessage(i18n("mine_is_not_exist"));
					}

				} else if (args[0].equalsIgnoreCase("broker")) {
					ItemStack stack = new ItemStack(Material.DIAMOND_PICKAXE);
					ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName("BROKER");
					List<String> lore = new ArrayList<String>();
					lore.add("misat11.mine.broker");
					meta.setLore(lore);
					stack.setItemMeta(meta);
					player.getInventory().addItem(stack);
					player.sendMessage(i18n("you_got_pickaxe"));
				} else if (args[0].equalsIgnoreCase("save") && args.length >= 2) {
					String name = args[1];
					if (Main.isMineExists(name)) {
						Mine mine = Main.getMine(name);
						mine.saveToConfig();
						player.sendMessage(i18n("mine_saved").replace("%name%", name));
					} else {
						player.sendMessage(i18n("mine_is_not_exist"));
					}

				} else if (args[0].equalsIgnoreCase("remove") && args.length >= 2) {
					String name = args[1];
					if (Main.isMineExists(name)) {
						Mine mine = Main.getMine(name);
						new File(Main.getInstance().getDataFolder(), "mines/" + name + ".yml").delete();
						Main.removeMine(mine);
						player.sendMessage(i18n("mine_was_removed").replace("%name%", name));
					} else {
						player.sendMessage(i18n("mine_is_not_exist"));
					}

				} else if (args[0].equalsIgnoreCase("regen") && args.length >= 2) {
					String name = args[1];
					if (Main.isMineExists(name)) {
						Mine mine = Main.getMine(name);
						for (MineBlock block : mine.getMineBlocks()) {
							try {
								block.spawnRandom(mine.getEntries(block.getEntrySet()), true);
							} catch (Throwable t) {
								
							}
						}
						player.sendMessage(i18n("mine_regen").replace("%name%", name));
					} else {
						player.sendMessage(i18n("mine_is_not_exist"));
					}
				} else if (args[0].equalsIgnoreCase("reload")) {
					Bukkit.getPluginManager().disablePlugin(Main.getInstance());
					Bukkit.getPluginManager().enablePlugin(Main.getInstance());
					player.sendMessage("Plugin reloaded!");
				} else {
					player.sendMessage(i18nonly("unknown_command"));
				}
			} else {
				sender.sendMessage(i18n("no_permissions"));
			}
		} else {
			sender.sendMessage("Miner commands cannot be executed from console!");
		}
		return true;
	}

	public void sendHelp(Player player) {
		player.sendMessage(i18nonly("help_title").replace("%version%", Main.getVersion()));
		player.sendMessage(i18nonly("help_mine_pos1"));
		player.sendMessage(i18nonly("help_mine_pos2"));
		player.sendMessage(i18nonly("help_mine_add"));
		player.sendMessage(i18nonly("help_mine_addtypelist"));
		player.sendMessage(i18nonly("help_mine_addentry"));
		player.sendMessage(i18nonly("help_mine_removeentry"));
		player.sendMessage(i18nonly("help_mine_removetypelist"));
		player.sendMessage(i18nonly("help_mine_build"));
		player.sendMessage(i18nonly("help_mine_broker"));
		player.sendMessage(i18nonly("help_mine_save"));
		player.sendMessage(i18nonly("help_mine_remove"));
		player.sendMessage(i18nonly("help_mine_regen"));
		player.sendMessage(i18nonly("help_mine_reload"));
	}

}
