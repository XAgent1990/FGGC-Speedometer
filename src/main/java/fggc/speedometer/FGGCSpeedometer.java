package fggc.speedometer;

import java.util.ArrayList;
import java.lang.Math;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import fggc.speedometer.util.FGGCLoadbar;
import fggc.speedometer.util.FGGCSpeedometerData;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class FGGCSpeedometer implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("fggc-speedometer");
	public static final String MOD_ID = "fggc-speedometer"; 

	private static final double DT = 1/20d;

	public static FGGCCommandCriterion COMMAND = Criteria.register(new FGGCCommandCriterion());
	public static FGGCSpeedCriterion SPEED = Criteria.register(new FGGCSpeedCriterion());

	static final byte ListSize = 20;
	static ArrayList<Double> vels, velsH, velsV;

	public enum SpeedometerMode{
		An,
		Flugmodus,
		Aus
	}

	public enum Speedtype{
		Total,
		Horizontal,
		Vertical
	}

	public enum Locomotion{
		Boat,
		Minecart,
		Elytra,
		Swimming,
		OnFoot,
		Falling,
		Any
	}

	private static PlayerList PL = new PlayerList();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		log("Initializing FGGC-Speedometer Mod!");

		vels = new ArrayList<Double>(ListSize);
		velsH = new ArrayList<Double>(ListSize);
		velsV = new ArrayList<Double>(ListSize);

		ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
			for(String name : server.getPlayerNames()){
				tickPlayer(server, server.getPlayerManager().getPlayer(name), name);
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			String name = player.getName().getString();
			SpeedometerMode mode = FGGCSpeedometerData.getMode(player);
			PL.setMode(name, mode);
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayerEntity player = handler.player;
			String name = player.getName().getString();
			SpeedometerMode mode = PL.getMode(PL.index(name));
			FGGCSpeedometerData.setMode(player, mode);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			final LiteralCommandNode<ServerCommandSource> tachoNode = dispatcher.register(literal("tacho")
				.executes(context -> {
					context.getSource().sendFeedback(Text.literal("Keine Argumente angegeben beim Tacho"), false);
					return Command.SINGLE_SUCCESS;
				})
				.then(argument("Modus", StringArgumentType.word())
				.suggests(new FGGCSuggestionProvider())
				.executes(context -> {
					String arg = StringArgumentType.getString(context, "Modus").toLowerCase();
					String name = context.getSource().getName();
					ServerPlayerEntity player = context.getSource().getPlayer();
					switch (arg) {
						case "aus":
							context.getSource().sendFeedback(Text.literal("Tacho ausgeschaltet."), false);
							PL.setMode(name, SpeedometerMode.Aus);
							break;
						case "flugmodus":
							context.getSource().sendFeedback(Text.literal("Tacho in Flugmodus geschaltet."), false);
							PL.setMode(name, SpeedometerMode.Flugmodus);
							COMMAND.trigger(player);
							break;
						case "an":
							context.getSource().sendFeedback(Text.literal("Tacho eingeschaltet."), false);
							PL.setMode(name, SpeedometerMode.An);
							COMMAND.trigger(player);
							break;
						default:
							return 1;
					}
					NbtCompound nbt = new NbtCompound();
					nbt.putString("TachoMode", PL.getMode(PL.index(name)).toString());
					player.writeCustomDataToNbt(nbt);
					return 1;
				}))
			);
			dispatcher.register(literal("Tacho").redirect(tachoNode));
		});
	}

	private static class PlayerTracker{
		String name;
		SpeedometerMode mode;
		double timer;
		double loadPercent;
		ArrayList<Double> velocities, velocitiesH, velocitiesV;
		Vec3d lastPos;
		PlayerTracker(String name){
			this.name = name;
			this.mode = SpeedometerMode.Aus;
			this.lastPos = Vec3d.ZERO;
			this.velocities = new ArrayList<Double>(ListSize+1);
			this.velocitiesH = new ArrayList<Double>(ListSize+1);
			this.velocitiesV = new ArrayList<Double>(ListSize+1);
		}
	}

	private static class PlayerList{
		ArrayList<PlayerTracker> pt;
		public PlayerList(){
			pt = new ArrayList<PlayerTracker>();
		}
		public byte append(String name, ServerPlayerEntity player){
			Vec3d pos = player.getPos();
			boolean exists = exists(name);
			if (!exists || size(index(name)) == 0) {
				if(!exists) create(name);
				byte i = (byte)(pt.size() - 1);
				add(i, 0d, 0d, 0d, pos);
				return i;
			}
			byte index = index(name);
			Vec3d last_pos = getLastPos(index);
			double distance = last_pos.distanceTo(pos);
			double distanceH = last_pos.distanceTo(new Vec3d(pos.x, last_pos.y, pos.z));
			double distanceV = pos.y < last_pos.y ? Math.abs(pos.y-last_pos.y) : 0d;
			double v = distance * 20;
			double vH = distanceH * 20;
			double vV = distanceV * 20;
			add(index, v, vH, vV, pos);
			pt.get(index).lastPos = pos;
			while(size(index) > ListSize)
				removeFirst(index);
			return index;
		}
		public double getAverageVelocity(byte index){
			@SuppressWarnings("unchecked")
			ArrayList<Double> velocities = (ArrayList<Double>)pt.get(index).velocities.clone();
			if (velocities.size() < ListSize) return 0d;
			velocities.sort(null);
			Double v1, v2, v3;
			v1 = velocities.get(ListSize/4);
			v2 = velocities.get(ListSize/2);
			v3 = velocities.get(3*ListSize/4);
			return (v1+v2+v3)/3;
		}
		public double getAverageVelocityH(byte index){
			@SuppressWarnings("unchecked")
			ArrayList<Double> velocitiesH = (ArrayList<Double>)pt.get(index).velocitiesH.clone();
			if (velocitiesH.size() < ListSize) return 0d;
			velocitiesH.sort(null);
			Double v1, v2, v3;
			v1 = velocitiesH.get(ListSize/4);
			v2 = velocitiesH.get(ListSize/2);
			v3 = velocitiesH.get(3*ListSize/4);
			return (v1+v2+v3)/3;
		}
		public double getAverageVelocityV(byte index){
			@SuppressWarnings("unchecked")
			ArrayList<Double> velocitiesV = (ArrayList<Double>)pt.get(index).velocitiesV.clone();
			if (velocitiesV.size() < ListSize) return 0d;
			velocitiesV.sort(null);
			Double v1, v2, v3;
			v1 = velocitiesV.get(ListSize/4);
			v2 = velocitiesV.get(ListSize/2);
			v3 = velocitiesV.get(3*ListSize/4);
			return (v1+v2+v3)/3;
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
		private void add(byte index, double velocity, double velocityH, double velocityV, Vec3d pos){
			pt.get(index).velocities.add(velocity);
			pt.get(index).velocitiesH.add(velocityH);
			pt.get(index).velocitiesV.add(velocityV);
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
			pt.get(index).velocitiesV.remove(0);
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

	private void tickPlayer(MinecraftServer server, ServerPlayerEntity player, String name){
		byte index = PL.append(name, player);
		if(player.isOnGround()) PL.resetTimer(index);
		else PL.addTimer(index, DT);
		if(PL.getMode(index) == SpeedometerMode.An ||
		  (PL.getMode(index) == SpeedometerMode.Flugmodus && PL.getTimer(index) >= 1)){

			// Total Velocity
			double f = 100d;
			double v = Math.round(PL.getAverageVelocity(index) * f)/f;
			if (vels.size() >= ListSize)
				vels.remove(0);
			vels.add(v);
			SPEED.trigger(player, Speedtype.Total, arrayListMin(vels), arrayListMax(vels));

			// Horizontal Velocity
			double vH = Math.round(PL.getAverageVelocityH(index) * f)/f;
			if (velsH.size() >= ListSize)
				velsH.remove(0);
			velsH.add(vH);
			SPEED.trigger(player, Speedtype.Horizontal, arrayListMin(velsH), arrayListMax(velsH));

			// Vertical Velocity
			double vV = Math.round(PL.getAverageVelocityV(index) * f)/f;
			if (velsV.size() >= ListSize)
				velsV.remove(0);
			velsV.add(vV);
			SPEED.trigger(player, Speedtype.Vertical, arrayListMin(PL.pt.get(index).velocitiesV), arrayListMax(PL.pt.get(index).velocitiesV));

			double loadPercent = PL.pt.get(index).loadPercent;
			MutableText text = Text.literal("");
			if(loadPercent > 0) text.append(Text.literal(FGGCLoadbar.LEFT.getLoadbar(loadPercent)).setStyle(color(loadPercent)));
			text.append(Text.literal(" Geschwindigkeit: " + String.format("%.2f", vH) + "m/s").setStyle(color(-1)));
			//if(!p.isOnGround())
			text.append(Text.literal(" | Fluggeschwindigkeit: " + String.format("%.2f", v) + "m/s ").setStyle(color(-1)));
			if(loadPercent > 0) text.append(Text.literal(FGGCLoadbar.RIGHT.getLoadbar(loadPercent)).setStyle(color(loadPercent)));
			player.sendMessage(text, true);
		}
	}

	private static double arrayListMin(ArrayList<Double> arr){
		if(arr == null || arr.size() < ListSize) return 0d;
		double min = arr.get(0);
		for(double x : arr)
			if(x < min) min = x;
		return min;
	}

	private static double arrayListMax(ArrayList<Double> arr){
		if(arr == null || arr.size() < ListSize) return 0d;
		double max = 0d;
		double y = 0d;
		for(double x : arr){
			if(x > 0 && x <= max) y = max;
			if(x > max) max = x;
		}
		return y > 0 ? max : 0d;
	}

	private static Style color(double percent){
		if(percent == -1) return Style.EMPTY.withColor(TextColor.parse("#ffffff"));
		String hex = "#";
		if(percent < 50) {
			hex += "ff";
			hex += percentToHex(percent, 0, 50, (short)0, (short)255);
		} else {
			hex += percentToHex(percent, 100, 50, (short)0, (short)255);
			hex += "ff";
		}
		hex += "00";
		return Style.EMPTY.withColor(TextColor.parse(hex));
	}

	private static String percentToHex(double percent, double percentMin, double percentMax, short hexMin, short hexMax){
		String hex = "";
		if(percentMin > percentMax){
			percent = percentMin + percentMax - percent;
			double temp = percentMin;
			percentMin = percentMax;
			percentMax = temp;
		}
		if(percent < percentMin) percent = percentMin;
		if(percent > percentMax) percent = percentMax;
		short i = (short)(Math.floor((percent - percentMin) * hexMax / (percentMax - percentMin)) + hexMin);
		short x = (short)Math.floorDiv(i, 16);
		short y = (short)Math.floorMod(i, 16);
		hex = Integer.toHexString(x) + Integer.toHexString(y);
		return hex;
	}

	public static void log(String text){
		LOGGER.info("[FGGC-Speedometer] " + text);
	}

	public static void setPercent(String name, double percent){
		PL.pt.get(PL.index(name)).loadPercent = percent;
	}
}