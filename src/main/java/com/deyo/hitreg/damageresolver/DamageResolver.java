package com.deyo.hitreg.damageresolver;

import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

public interface DamageResolver {

	double getDamage(Player p, Damageable entity);

	static DamageResolver getDamageResolver(boolean USE_CRITS) {
		try {
			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];

			switch(version) {

				case "v1_8_R3": return new DamageResolver_1_8_R3(USE_CRITS);

				default: return null;
			}

		} catch(ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("Error while registering damage resolver (unknown version '" + Bukkit.getServer().getClass().getPackage().getName() + "'?): ", e);
		}
	}

}
