package fggc.speedometer;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import fggc.speedometer.FGGCSpeedometer.Locomotion;
import fggc.speedometer.FGGCSpeedometer.Speedtype;
import fggc.speedometer.util.FGGCSpeedometerData;

public class FGGCSpeedCriterion extends AbstractCriterion<FGGCSpeedCriterion.Conditions> {
    static final Identifier ID = new Identifier(FGGCSpeedometer.MOD_ID, "speed");

    public static class Conditions extends AbstractCriterionConditions {

        private static final double DT = 1/20d;
        final String id;
        final double triggerTime;
        ArrayList<String> players, initialized;
        double minimumSpeed;
        double maximumSpeed;
        Speedtype speedtype;
        Locomotion locomotion;
        double timer;

        public Conditions(Extended entity, String id, Speedtype speedtype, double minimumSpeed, double maximumSpeed, Locomotion locomotion, double triggerTime) {
            super(new Identifier(FGGCSpeedometer.MOD_ID, "speed"), entity);
            this.id = id;
            this.triggerTime = triggerTime;
            this.players = new ArrayList<String>();
            this.initialized = new ArrayList<String>();
            this.minimumSpeed = minimumSpeed;
            this.maximumSpeed = maximumSpeed;
            this.speedtype = speedtype;
            this.locomotion = locomotion;
        }
 
        boolean requirementsMet(ServerPlayerEntity player, Speedtype speedtype, double minSpeed, double maxSpeed) {
            if(this.speedtype != speedtype) return false;
            String name = player.getName().getString();
            if(!initialized.contains(name)){
                if(FGGCSpeedometerData.hasAdvancement(player, id)) players.add(name);
                initialized.add(name);
            }
            if(players.contains(name)) return true;
            boolean speedReached = minSpeed >= minimumSpeed;
            boolean speedExceded = (maximumSpeed > 0) ? (maxSpeed > maximumSpeed) : false;
            boolean reqMet = ((locomotion == Locomotion.Falling) ? (maxSpeed > minimumSpeed) : (speedReached && !speedExceded));
            boolean loco = locomotionFullfilled(player, locomotion);
            boolean ret = timerReached(player, reqMet, loco);
            if(!loco) return false;
            if(ret){
                players.add(name);
                FGGCSpeedometerData.setAdvancement(player, id);
            }
            return ret;
        }

        private boolean timerReached(ServerPlayerEntity player, boolean reqMet, boolean loco){
            if(reqMet && loco){
                if((timer += DT) >= triggerTime){
                    FGGCSpeedometer.setPercent(player.getName().getString(), timer = 0);
                    return true;
                } else {
                    FGGCSpeedometer.setPercent(player.getName().getString(), 100 * timer / triggerTime);
                    return false;
                }
            } else {
                if(timer == 0d) return false;
                if((timer -= DT) < 0d) timer = 0d;
                FGGCSpeedometer.setPercent(player.getName().getString(), 100 * timer / triggerTime);
                return false;
            }
        }

        private boolean locomotionFullfilled(ServerPlayerEntity player, Locomotion locomotion){
            switch (locomotion) {
                case Boat:
                    if(!(player.getVehicle() instanceof BoatEntity)) return false;
                    break;
                case Minecart:
                    if(!(player.getVehicle() instanceof MinecartEntity)) return false;
                    break;
                case Elytra:
                    if(!(player.isFallFlying())) return false;
                    break;
                case Swimming:
                    if(!(player.isSwimming())) return false;
                    break;
                case OnFoot:
                    if(!(player.isOnGround())) return false;
                    break;
                case Falling:
                    if (player.isFallFlying() || 
                        player.isSwimming() || 
                        player.getVehicle() instanceof Entity) return false;
                    break;
                default:
                    break;
            }
            return true;
        }
        
        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject json = super.toJson(predicateSerializer);
            json.addProperty("id", this.id);
            json.addProperty("minimum_speed", this.minimumSpeed);
            json.addProperty("maximum_speed", this.maximumSpeed);
            json.addProperty("speedtype", this.speedtype.name());
            json.addProperty("locomotion", this.locomotion.name());
            json.addProperty("trigger_time", this.triggerTime);
            return json;
        }
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Extended playerPredicate,
            AdvancementEntityPredicateDeserializer predicateDeserializer) {
        String id = obj.get("id").getAsString();
        double minimumSpeed = obj.get("minimum_speed").getAsDouble();
        double maximumSpeed = obj.get("maximum_speed").getAsDouble();
        Speedtype speedtype = Speedtype.valueOf(obj.get("speedtype").getAsString());
        Locomotion locomotion = Locomotion.valueOf(obj.get("locomotion").getAsString());
        double triggerTime = obj.get("trigger_time").getAsDouble();
        Conditions conditions = new Conditions(playerPredicate, id, speedtype, minimumSpeed, maximumSpeed, locomotion, triggerTime);
        return conditions;
    }

    protected void trigger(ServerPlayerEntity player, Speedtype speedtype, double minSpeed, double maxSpeed){
        trigger(player, conditions -> {
            return conditions.requirementsMet(player, speedtype, minSpeed, maxSpeed);
        });
    }
}
