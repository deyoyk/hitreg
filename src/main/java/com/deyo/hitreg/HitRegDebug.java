package com.deyo.hitreg;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

class HitRegDebug implements Listener {
	private HitReg plugin;

	HitRegDebug(HitReg pl) {
		plugin = pl;
	}


	@EventHandler (priority = EventPriority.LOWEST)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
		if( e.getDamager() instanceof Player && e.getEntity() instanceof Player ) {
			plugin.getLogger().info( " --- Hitreg Debug --- " );
			plugin.getLogger().info( "Attacker: " + ((Player)e.getDamager()).getName() + ", victim: " + ((Player)e.getEntity()).getName() );
			plugin.getLogger().info( "Final damage: " + e.getFinalDamage() );
		}
	}
}