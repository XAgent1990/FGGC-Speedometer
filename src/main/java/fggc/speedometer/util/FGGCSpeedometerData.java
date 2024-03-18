package fggc.speedometer.util;

import fggc.speedometer.FGGCSpeedometer.SpeedometerMode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class FGGCSpeedometerData {
    private static String modeName = "Tacho";
    private static String advancementsName = "Advancements";
    
    public static void setMode(ServerPlayerEntity player, SpeedometerMode mode){
        IEntityDataSaver playerData = (IEntityDataSaver)player;
        playerData.getPersistentData().putString(modeName, mode.name());
    }

    public static SpeedometerMode getMode(ServerPlayerEntity player){
        IEntityDataSaver playerData = (IEntityDataSaver)player;
        NbtCompound nbt = playerData.getPersistentData();
        if(nbt.contains(modeName))
            return SpeedometerMode.valueOf(nbt.getString(modeName));
        else
            return SpeedometerMode.Aus;
    }

    public static void setAdvancement(ServerPlayerEntity player, String advName){
        IEntityDataSaver playerData = (IEntityDataSaver)player;
        NbtCompound data = playerData.getPersistentData();
        NbtCompound nbt = data.contains(advancementsName) && data.get(advancementsName) instanceof NbtCompound ? (NbtCompound)data.get(advancementsName) : new NbtCompound();
        nbt.putBoolean(advName, true);
        data.put(advancementsName, nbt);
    }

    public static boolean hasAdvancement(ServerPlayerEntity player, String advName){
        IEntityDataSaver playerData = (IEntityDataSaver)player;
        NbtCompound data = playerData.getPersistentData();
        if(!data.contains(advancementsName) || !(data.get(advancementsName) instanceof NbtCompound)) return false;
        return ((NbtCompound)data.get(advancementsName)).contains(advName);
    }
}
