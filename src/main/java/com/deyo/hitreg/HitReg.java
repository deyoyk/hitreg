package com.deyo.hitreg;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncListenerHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Hitreg - Async hit preprocessor for the Bukkit API
 * Catches hit request packets asynchronously and processes
 * them in an async thread
 *
 * @author frash23 / Jacob Pedersen
 * @version 0.5b
 *
 * Licensed under <a href="https://github.com/frash23/hitreg/blob/master/LICENSE">NBPL v2</a>
 */
public class HitReg extends JavaPlugin implements Listener {
	private boolean listening = false;
	private boolean debugging = false;
	private static HitReg instance;
	private HitListener hitListener = null;
	private HitRegDebug debugListener = null;
	private AsyncListenerHandler hitListenerHandler;
	private ProtocolManager pmgr;

	@Override
	public void onEnable() {
		instance = this;
		pmgr = ProtocolLibrary.getProtocolManager();

		getCommand("hitreg").setExecutor( new HitRegCommand(this) );
		reload();

		// No background watcher; toggle command drives reload
	}

	@Override
	public void onDisable() {
		if( getHitListener() != null) unregisterHitListener();

		pmgr = null;
		instance = null;
	}

	void registerHitListener() {

		if( getHitListener() == null) {


			/* We're doing this in a separate thread as we want instantiation in the same thread as the listener itself */
			new BukkitRunnable() {
				@Override public void run() {
					setHitListener( new HitListener(
							instance,
							getConfig().getBoolean("enable-criticals")
					) );

					setHitListenerHandler( pmgr.getAsynchronousManager().registerAsyncHandler( getHitListener() ) );
					getHitListenerHandler().start();
				}
			}.runTaskAsynchronously(this);

			listening = true;
		}
	}

	void unregisterHitListener() {
		if( getHitListener() != null) {
			pmgr.getAsynchronousManager().unregisterAsyncHandler( getHitListenerHandler() );
			listening = false;
			setHitListener(null);
		}
	}

	void registerDebugListener() {
		if(debugListener == null) debugListener = new HitRegDebug(this);
		getServer().getPluginManager().registerEvents(debugListener, this);
		debugging = true;
	}
	void unregisterDebugListener() {
		if(debugListener != null) {
			HandlerList.unregisterAll(debugListener);
			debugging = false;
			debugListener = null;
		}
	}



	void reload() {
		saveDefaultConfig();
		reloadConfig();

		if( getHitListener() != null ) getHitListener().stop();
		unregisterHitListener();
		if( getConfig().getBoolean("enabled", true) ) {
			registerHitListener();
		}



	}

	/* These are synchronized as we're setting it from another thread */
	private synchronized HitListener getHitListener() { return hitListener; }
	private synchronized void setHitListener(HitListener hl) { hitListener = hl; }
	private synchronized AsyncListenerHandler getHitListenerHandler() { return hitListenerHandler; }
	private synchronized void setHitListenerHandler(AsyncListenerHandler hl) { hitListenerHandler = hl; }

	boolean isListening() { return listening; }
	boolean isDebug() { return debugging; }

	static HitReg getInstance() { return instance; }
}
