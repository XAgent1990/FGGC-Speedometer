package fggc.speedometer.util;

import fggc.speedometer.FGGCSpeedometer.SpeedometerMode;
import net.minecraft.nbt.NbtCompound;

public class FGGCSpeedometerData {
    private static String modeName = "Tacho";
    
    public static void setMode(IEntityDataSaver player, SpeedometerMode mode){
        player.getPersistentData().putString(modeName, mode.name());
    }

    public static SpeedometerMode getMode(IEntityDataSaver player){
        NbtCompound nbt = player.getPersistentData();
        if(nbt.contains(modeName))
            return SpeedometerMode.valueOf(nbt.getString(modeName));
        else
            return SpeedometerMode.Aus;
    }
}
