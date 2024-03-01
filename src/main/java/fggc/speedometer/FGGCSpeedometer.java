package fggc.speedometer;

import java.util.ArrayList;
import java.lang.Math;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class FGGCSpeedometer implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("fggc-speedometer");
	static final byte ListSize = 10;

	public static long lastTimestamp = Util.getMeasuringTimeMs();

	private enum SpeedometerMode{
		An,
		Flugmodus,
		Aus
	}

	private static class PlayerTracker{
		String name;
		public ServerPlayerEntity player;
		SpeedometerMode mode;
		// boolean grounded;
		ArrayList<Double> velocities, velocitiesH;
		Vec3d lastPos;
		PlayerTracker(String name){
			this.name = name;
			this.mode = SpeedometerMode.Aus;
			// this.grounded = true;
			this.lastPos = Vec3d.ZERO;
			this.velocities = new ArrayList<Double>(ListSize);
			this.velocitiesH = new ArrayList<Double>(ListSize);
		}
	}

	private static class PlayerList{
		ArrayList<PlayerTracker> pt;
		public PlayerList(){
			pt = new ArrayList<PlayerTracker>();
		}
		public void append(String name, Vec3d pos){
			boolean exists = exists(name);
			if (!exists || size(index(name)) == 0) {
				if(!exists) create(name);
				add((byte)(pt.size() - 1), 0d, 0d);
				return;
			}
			byte index = index(name);
			Vec3d last_pos = getLastPos(index);
			double distanceH = Math.sqrt(Math.pow((pos.x-last_pos.x),2)+
										 Math.pow((pos.z-last_pos.z),2));
			double distance = Math.sqrt(Math.pow((pos.x-last_pos.x),2)+
						  	  			Math.pow((pos.y-last_pos.y),2)+
						  	 			Math.pow((pos.z-last_pos.z),2));
			long timeDelta = Util.getMeasuringTimeMs() - lastTimestamp;
			double v = distance * 1000d / timeDelta;
			double vH = distanceH * 1000d / timeDelta;
			add(index, v, vH);
			pt.get(index).lastPos = pos;
			while(size(index) > ListSize)
				removeFirst(index);
		}
		// public boolean isGrounded(String name){
		// 	if(exists(name))
		// 		return pt.get(index(name)).grounded;
		// 	return true;
		// }
		// public void reset(String name){
		// 	if(exists(name)){
		// 		byte index = index(name);
		// 		LOGGER.info("Landed!");
		// 		pt.get(index).velocities.clear();
		// 		pt.get(index).grounded = true;
		// 	}
		// }
		public double getAverageVelocity(String name){
			if(!exists(name)) return 0;
			byte index = index(name);
			double v = 0;
			for(Double item : pt.get(index).velocities)
				v += item;
			return v / ListSize;
		}
		public double getAverageVelocityH(String name){
			if(!exists(name)) return 0;
			byte index = index(name);
			double v = 0;
			for(Double item : pt.get(index).velocitiesH)
				v += item;
			return v / ListSize;
		}
		public SpeedometerMode getMode(String name){
			if(!exists(name)) return SpeedometerMode.Aus;
			return pt.get(index(name)).mode;
		}
		public void setMode(String name, SpeedometerMode mode){
			if(!exists(name)) create(name);
			pt.get(index(name)).mode = mode;
		}
		public void setPlayer(String name, ServerPlayerEntity player){
			if(!exists(name)) create(name);
			pt.get(index(name)).player = player;
		}
		public ServerPlayerEntity getPlayer(String name){
			if(!exists(name)) return null;
			return pt.get(index(name)).player;
		}
		private boolean exists(String name){
			for(PlayerTracker item : pt){
				if(!item.name.equals(name)) continue;
				return true;
			}
			return false;
		}
		private void create(String name){
			pt.add(new PlayerTracker(name));
			LOGGER.info("Registered new Player '" + name + "'' to Speedometerlist!");
		}
		private void add(byte index, Double velocity, double velocityH){
			pt.get(index).velocities.add(velocity);
			pt.get(index).velocitiesH.add(velocityH);
			// pt.get(index).grounded = false;
		}
		private Vec3d getLastPos(byte index){
			return pt.get(index).lastPos;
		}
		private byte size(byte index){
			return (byte)pt.get(index).velocities.size();
		}
		private void removeFirst(byte index){
			pt.get(index).velocities.remove(0);
			pt.get(index).velocitiesH.remove(0);
		}
		private byte index(String name){
			for(byte i = 0; i < pt.size(); i++){
				if(!pt.get(i).name.equals(name)) continue;
				return i;
			}
			return -1;
		}
	}

	private static PlayerList PL = new PlayerList();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Initializing FGGC-Speedometer Mod!");
		ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
			for(String name : server.getPlayerNames()){
				tickPlayer(server, server.getPlayerManager().getPlayer(name), name);
			}
			lastTimestamp = Util.getMeasuringTimeMs();
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> 
			dispatcher.register(literal("tacho")
				.executes(context -> {
					context.getSource().sendFeedback(Text.literal("Keine Argumente angegeben beim Tacho"), false);
					return Command.SINGLE_SUCCESS;
				})
				.then(argument("Modus", StringArgumentType.word())
				.suggests(new FGGCSuggestionProvider())
				.executes(context -> {
					String arg = StringArgumentType.getString(context, "Modus").toLowerCase();
					String name = context.getSource().getDisplayName().getString();
					ServerPlayerEntity player = context.getSource().getPlayer();
					PL.setPlayer(name, player);
					switch (arg) {
						case "aus":
							context.getSource().sendFeedback(Text.literal("Tacho ausgeschaltet."), false);
							PL.setMode(name, SpeedometerMode.Aus);
							break;
						case "flugmodus":
							context.getSource().sendFeedback(Text.literal("Tacho in Flugmodus geschaltet."), false);
							PL.setMode(name, SpeedometerMode.Flugmodus);
							break;
						case "an":
							context.getSource().sendFeedback(Text.literal("Tacho eingeschaltet."), false);
							PL.setMode(name, SpeedometerMode.An);
							break;
						default:
							break;
					}
					return 1;
				}))
			)
		);
	}

	private void tickPlayer(MinecraftServer server, ServerPlayerEntity player, String name){
		PL.append(name, player.getPos());
		if(PL.getMode(name) == SpeedometerMode.An ||
		  (PL.getMode(name) == SpeedometerMode.Flugmodus && player.isFallFlying())){
			double v = Math.round(PL.getAverageVelocity(name) * 10)/10d;
			double vH = Math.round(PL.getAverageVelocityH(name) * 10)/10d;
			String text = "Geschwindigkeit: " + v + "m/s";
			//if(!p.isOnGround())
			text += " | Fluggeschwindigkeit: " + vH + "m/s";
			player.sendMessage(Text.literal(text), true);
		}
	}
}