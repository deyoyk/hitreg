package com.deyo.hitreg;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

class HitRegCommand implements CommandExecutor {
	private HitReg plugin;

	HitRegCommand(HitReg pl) {
		plugin = pl;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		/* You can pry the section signs from my cold, dead hands */

		String subcmd = args.length < 1?  "help" : args[0];
		switch( subcmd.toLowerCase() ) {
			case "reload":
				plugin.reload();
				sender.sendMessage("Hitreg reloaded");
			return true;

			case "toggle":
				if( plugin.isListening() ) {
					plugin.unregisterHitListener();
					plugin.getConfig().set("enabled", false);
					plugin.saveConfig();
					plugin.reload();
					sender.sendMessage("Hitreg disabled and reloaded");
				} else {
					plugin.registerHitListener();
					plugin.getConfig().set("enabled", true);
					plugin.saveConfig();
					plugin.reload();
					sender.sendMessage("Hitreg enabled and reloaded");
				}
			return true;

			case "debug":
				if( plugin.isDebug() ) {
					plugin.unregisterDebugListener();
					sender.sendMessage("Hitreg debug disabled");
				} else {
					plugin.registerDebugListener();
					sender.sendMessage("Hitreg debug enabled");
				}
			return true;

			default:
				sender.sendMessage(new String[]{
						"Hitreg v1.0-RC1",
						"/hitreg help - Show this message",
						"/hitreg reload - Reload config & restart hit interception",
						"/hitreg toggle - Disable/enable hit interception"
				});
			return true;
		}
	}
}
