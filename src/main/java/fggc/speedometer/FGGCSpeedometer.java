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
	static final byte ListSize = 20;

	public static long lastTimestamp = Util.getMeasuringTimeMs();

	private enum SpeedometerMode{
		An,
		Flugmodus,
		Aus
	}

	private static class PlayerTracker{
		String name;
		SpeedometerMode mode;
		double timer;
		long deltaTimeMs;
		// boolean grounded;
		ArrayList<Double> velocities, velocitiesH;
		ArrayList<Vec3d> positions;
		ArrayList<Long> timesMs;
		Vec3d lastPos;
		PlayerTracker(String name){
			this.name = name;
			this.mode = SpeedometerMode.Aus;
			// this.grounded = true;
			this.lastPos = Vec3d.ZERO;
			this.velocities = new ArrayList<Double>(ListSize+1);
			this.velocitiesH = new ArrayList<Double>(ListSize+1);
			this.positions = new ArrayList<Vec3d>(ListSize+1);
			this.timesMs = new ArrayList<Long>(ListSize+1);
		}
	}

	private static class PlayerList{
		ArrayList<PlayerTracker> pt;
		public PlayerList(){
			pt = new ArrayList<PlayerTracker>();
		}
		public byte append(String name, Vec3d pos){
			boolean exists = exists(name);
			if (!exists || size(index(name)) == 0) {
				if(!exists) create(name);
				byte i = (byte)(pt.size() - 1);
				add(i, 0d, 0d, pos, Util.getMeasuringTimeMs());
				return i;
			}
			byte index = index(name);
			Vec3d last_pos = getLastPos(index);
			double distanceH = Math.sqrt(Math.pow((pos.x-last_pos.x),2)+
										 Math.pow((pos.z-last_pos.z),2));
			double distance = Math.sqrt(Math.pow((pos.x-last_pos.x),2)+
						  	  			Math.pow((pos.y-last_pos.y),2)+
						  	 			Math.pow((pos.z-last_pos.z),2));
			long deltaTime = Util.getMeasuringTimeMs() - lastTimestamp;
			pt.get(index).deltaTimeMs = deltaTime;
			double v = distance * 1000d / deltaTime;
			double vH = distanceH * 1000d / deltaTime;
			add(index, v, vH, pos, Util.getMeasuringTimeMs());
			pt.get(index).lastPos = pos;
			while(size(index) > ListSize)
				removeFirst(index);
			return index;
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
		public double getAverageVelocity(byte index){
			//double v = 0;
			//for(double item : pt.get(index).velocities)
			//	v += item;
			//return v / ListSize;
			@SuppressWarnings("unchecked")
			ArrayList<Double> velocities = (ArrayList<Double>)pt.get(index).velocities.clone();
			velocities.sort(null);
			Double v1, v2, v3;
			v1 = velocities.get(ListSize/4);
			v2 = velocities.get(ListSize/2);
			v3 = velocities.get(3*ListSize/4);
			return (v1+v2+v3)/3;
		}
		public double getAverageVelocityH(byte index){
			//double v = 0;
			//for(double item : pt.get(index).velocitiesH)
			//	v += item;
			//return v / ListSize;
			@SuppressWarnings("unchecked")
			ArrayList<Double> velocitiesH = (ArrayList<Double>)pt.get(index).velocitiesH.clone();
			velocitiesH.sort(null);
			Double v1, v2, v3;
			v1 = velocitiesH.get(ListSize/4);
			v2 = velocitiesH.get(ListSize/2);
			v3 = velocitiesH.get(3*ListSize/4);
			return (v1+v2+v3)/3;
		}
		@SuppressWarnings("unused")
		public double getVelocity(byte index, Vec3d pos){
			Vec3d last_pos = pt.get(index).positions.get(0);
			double distance = Math.sqrt(Math.pow((pos.x-last_pos.x),2)+
						  	  			Math.pow((pos.y-last_pos.y),2)+
						  	 			Math.pow((pos.z-last_pos.z),2));
			double v = distance * 1000d / (Util.getMeasuringTimeMs() - pt.get(index).timesMs.get(0));
			return v;
		}
		@SuppressWarnings("unused")
		public double getVelocityH(byte index, Vec3d pos){
			Vec3d last_pos = pt.get(index).positions.get(0);
			double distanceH = Math.sqrt(Math.pow((pos.x-last_pos.x),2)+
										 Math.pow((pos.z-last_pos.z),2));
			double vH = distanceH * 1000d / (Util.getMeasuringTimeMs() - pt.get(index).timesMs.get(0));
			return vH;
		}
		public SpeedometerMode getMode(byte index){
			return pt.get(index).mode;
		}
		public void setMode(String name, SpeedometerMode mode){
			pt.get(index(name)).mode = mode;
		}
		public void addTimer(byte index, double dt){ pt.get(index).timer += dt; }
		public void resetTimer(byte index){ pt.get(index).timer = 0d; }
		public double getTimer(byte index){ return pt.get(index).timer; }
		public Long getDeltaTimeMs(byte index){ return pt.get(index).deltaTimeMs; }
		private boolean exists(String name){
			for(PlayerTracker item : pt){
				if(!item.name.equals(name)) continue;
				return true;
			}
			return false;
		}
		private void create(String name){
			pt.add(new PlayerTracker(name));
			log("Registered new Player '" + name + "' to Speedometerlist!");
		}
		private void add(byte index, double velocity, double velocityH, Vec3d pos, Long timeMs){
			pt.get(index).velocities.add(velocity);
			pt.get(index).velocitiesH.add(velocityH);
			pt.get(index).positions.add(pos);
			pt.get(index).timesMs.add(timeMs);
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
			pt.get(index).positions.remove(0);
			pt.get(index).timesMs.remove(0);
		}
		private byte index(String name){
			byte i;
			for(i = 0; i < pt.size(); i++){
				if(!pt.get(i).name.equals(name)) continue;
				return i;
			}
			create(name);
			return i;
		}
	}

	private static PlayerList PL = new PlayerList();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		log("Initializing FGGC-Speedometer Mod!");
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
					String name = context.getSource().getName();
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
		byte index = PL.append(name, player.getPos());
		if(player.isOnGround()) PL.resetTimer(index);
		else PL.addTimer(index, PL.getDeltaTimeMs(index) / 1000d);
		if(PL.getMode(index) == SpeedometerMode.An ||
		  (PL.getMode(index) == SpeedometerMode.Flugmodus && PL.getTimer(index) >= 1)){
			double v = Math.round(PL.getAverageVelocity(index) * 10)/10d;
			double vH = Math.round(PL.getAverageVelocityH(index) * 10)/10d;
			//double v = Math.round(PL.getVelocity(index, player.getPos()) * 10)/10d;
			//double vH = Math.round(PL.getVelocityH(index, player.getPos()) * 10)/10d;
			String text = "Geschwindigkeit: " + vH + "m/s";
			//if(!p.isOnGround())
			text += " | Fluggeschwindigkeit: " + v + "m/s";
			player.sendMessage(Text.literal(text), true);
		}
	}

	private static void log(String text){
		LOGGER.info("[FGGC-Speedometer] " + text);
	}
}