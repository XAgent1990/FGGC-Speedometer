package fggc.speedometer.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fggc.speedometer.FGGCSpeedometer;
import fggc.speedometer.util.IEntityDataSaver;

@Mixin(Entity.class)
public abstract class FGGCEntityDataSaverMixin implements IEntityDataSaver {
    private NbtCompound persistentData;
    private static String nbtName = FGGCSpeedometer.MOD_ID + ":SpeedometerData";

    @Override
    public NbtCompound  getPersistentData(){
        if (this.persistentData == null)
            this.persistentData = new NbtCompound();

        return persistentData;
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    protected void injectWriteMethod(NbtCompound nbt, CallbackInfoReturnable info){
        if (persistentData != null)
            nbt.put(nbtName, persistentData);
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    protected void injectReadMethod(NbtCompound nbt, CallbackInfo info){
        if (nbt.contains(nbtName, 10))
            persistentData = nbt.getCompound(nbtName);
    }
}
