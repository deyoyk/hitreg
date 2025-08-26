package com.deyo.hitreg.damageresolver;

import net.minecraft.server.v1_8_R3.GenericAttributes;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

class DamageResolver_1_8_R3 implements DamageResolver {

	private boolean USE_CRITS;

	DamageResolver_1_8_R3(boolean useCrits) {
		USE_CRITS = useCrits;
	}

	@Override
	public double getDamage(Player damager, Damageable entity) {
		double damage = ((CraftPlayer)damager).getHandle().getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
		if(USE_CRITS
		&& !( (Entity)damager ).isOnGround()
		&& damager.getVelocity().getY() < 0
		&& !damager.isSprinting()
		) damage *= 1.5;

		return damage;
	}

}
