package blockcrafting;

import cn.nukkit.plugin.*;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.*;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.item.Item;

public class Main extends PluginBase implements Listener{
	private Config banDB;
	@Override
	public void onEnable() {
		loadDB();
		getServer().getPluginManager().registerEvents(this, this);
	}
	@Override
	public void onDisable() {
		banDB.save();
	}
	public void loadDB() {
		getDataFolder().mkdirs();
		banDB = new Config(getDataFolder() + "/banDB.json", Config.JSON);
	}
	@EventHandler
	public void onCraft(CraftItemEvent event) {
		Player player = event.getPlayer();
		Item item = event.getRecipe().getResult();
		if (player.hasPermission("blockcrafting.craft")) {
			return;
		}
		if (banDB.getAll().containsValue(String.valueOf(item.getId()))) {
			event.setCancelled();
			player.sendPopup(TextFormat.RED + "You can't craft this item.");
		}
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}
		switch (args[0]) {
		case "add" :
			if (!sender.hasPermission("blockcrafting.commands.add")) {
				sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.permission"));
				return true;
			}
			if (args.length < 2) {
				sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.usage", "/" + command.getName() + " add <itemid>"));
				return true;
			}
			try {
				Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(TextFormat.RED + "Itemid must be input integer.");
				return true;
			}
			banDB.set(String.valueOf(banDB.getAll().values().size()), args[1]);
			sender.sendMessage("Now, players can't craft itemid: " + args[1]);
			return true;
		case "delete" :
			if (!sender.hasPermission("blockcrafting.commands.delete")) {
				sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.permission"));
				return true;
			}
			if (args.length < 2) {
				sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.usage", "/" + command.getName() + " delete <itemid>"));
				return true;
			}
			if (!banDB.getAll().containsValue(args[1])) {
				sender.sendMessage("That item isn't blocked");
				return true;
			}
			for (int i = 0; i < banDB.getAll().values().size(); i++) {
				if (banDB.get(String.valueOf(i)).equals(args[1])) {
					banDB.remove(String.valueOf(i));
				}
			}
			sender.sendMessage("Now, players can craft itemid: " + args[1]);
			return true;
		case "list" :
			String list = null;
			sender.sendMessage("block crafting list:");
			for (Object block : banDB.getAll().values()) {
				if (list != null) {
					list += (String)block + ", ";
				} else {
					list = (String)block + ", ";
				}
			}
			if (list != null) {
				sender.sendMessage(list);
			} else {
				sender.sendMessage("This server doesn't have block crafting list");
			}
			return true;
		default :
			return false;
		}
	}
}
