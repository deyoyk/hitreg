package com.deyo.hitreg;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.deyo.hitreg.damageresolver.DamageResolver;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.bukkit.Bukkit.getPluginManager;

class HitListener extends PacketAdapter {
	private HitReg plugin;
	private ProtocolManager pmgr;
	private DamageResolver damageResolver;

	private Queue<EntityDamageByEntityEvent> hitQueue = new ConcurrentLinkedQueue<>();

	HitListener(HitReg pl, boolean useCrits) {
		super(pl, ListenerPriority.HIGH, Collections.singletonList( PacketType.Play.Client.USE_ENTITY) );

		plugin = pl;
		pmgr = ProtocolLibrary.getProtocolManager();

		damageResolver = DamageResolver.getDamageResolver(useCrits);
		if(damageResolver == null) throw new NullPointerException("Damage resolver is null, unsupported Spigot version?");
	}

	private BukkitTask hitQueueProcessor = new BukkitRunnable() {
		@Override
		public void run() {
			while( hitQueue.size() > 0 ) {
				EntityDamageByEntityEvent e = hitQueue.remove();
				getPluginManager().callEvent(e);
				if( !e.isCancelled() ) {
					Player attacker = (Player) e.getDamager();
					PacketContainer damageAnimation = new PacketContainer(PacketType.Play.Server.ENTITY_STATUS);
					damageAnimation.getIntegers().write(0, e.getEntity().getEntityId());
					damageAnimation.getBytes().write(0, (byte)2);
					
					pmgr.sendServerPacket(attacker, damageAnimation);
					
					( (Damageable)e.getEntity() ).damage( e.getFinalDamage(), e.getDamager() );
				}
			}
		}
	}.runTaskTimer(HitReg.getInstance(), 1, 1);

	@SuppressWarnings("deprecation")
	@Override
	public void onPacketReceiving(PacketEvent e) {

		PacketContainer packet = e.getPacket();
		Player attacker = e.getPlayer();
		Entity entity = packet.getEntityModifier(e).read(0);
		Damageable target = entity instanceof Damageable? (Damageable)entity : null;
		World world = attacker.getWorld();

		/* Huge if() block to verify the hit request */
		if(e.getPacketType() == PacketType.Play.Client.USE_ENTITY			// Packet is for entity interaction
		&& packet.getEntityUseActions().read(0) == EntityUseAction.ATTACK	// Packet is for entity damage
		&& target != null && !target.isDead()									// Target entity is damageable
		&& world == target.getWorld() && world.getPVP() 						// Attacker & target are in the same world
		&& (!(target instanceof Player) || ((Player) target).getGameMode() != GameMode.CREATIVE)) { // Don't hit Players in creative mode

			/* The check above ensures we can roll our own hits */
			e.setCancelled(true);

			double damage = damageResolver.getDamage(attacker, target);

			AsyncPreDamageEvent damageEvent = new AsyncPreDamageEvent(attacker, target, damage);
			getPluginManager().callEvent(damageEvent);

			if( !damageEvent.isCancelled() ) {
				// Queue the hit without CPS limiting
				hitQueue.add( new EntityDamageByEntityEvent( attacker, target, DamageCause.ENTITY_ATTACK, damageEvent.getDamage() ) );
			}
		}
	}

	void stop() {
		hitQueueProcessor.cancel();
		damageResolver = null;
	}
}
