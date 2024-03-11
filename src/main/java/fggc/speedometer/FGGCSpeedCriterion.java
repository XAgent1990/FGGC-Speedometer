package fggc.speedometer;

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
import static fggc.speedometer.FGGCSpeedometer.log;

public class FGGCSpeedCriterion extends AbstractCriterion<FGGCSpeedCriterion.Conditions> {
    static final Identifier ID = new Identifier("fggc-speedometer","speed");

    public static class Conditions extends AbstractCriterionConditions {

        double minimumSpeed;
        double maximumSpeed;
        Speedtype speedtype;
        Locomotion locomotion;

        public Conditions(Extended entity, Speedtype speedtype, double minimumSpeed, double maximumSpeed, Locomotion locomotion) {
            super(FGGCSpeedCriterion.ID, entity);
            this.minimumSpeed = minimumSpeed;
            this.maximumSpeed = maximumSpeed;
            this.speedtype = speedtype;
            this.locomotion = locomotion;
        }
 
        boolean requirementsMet(ServerPlayerEntity player, Speedtype speedtype, double minSpeed, double maxSpeed) {
            if(this.speedtype != speedtype) return false;
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
                    return maxSpeed > minimumSpeed;
                default:
                    break;
            }
            boolean speedReached = minSpeed >= minimumSpeed;
            boolean speedExceded = (maximumSpeed > 0) ? (maxSpeed > maximumSpeed) : false;
            return speedReached && !speedExceded;
        }
        
        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject json = super.toJson(predicateSerializer);
            json.addProperty("minimum_speed", this.minimumSpeed);
            json.addProperty("maximum_speed", this.maximumSpeed);
            json.addProperty("speedtype", this.speedtype.name());
            json.addProperty("locomotion", this.locomotion.name());
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
        double minimumSpeed = obj.get("minimum_speed").getAsDouble();
        double maximumSpeed = obj.get("maximum_speed").getAsDouble();
        Speedtype speedtype = Speedtype.valueOf(obj.get("speedtype").getAsString());
        Locomotion locomotion = Locomotion.valueOf(obj.get("locomotion").getAsString());
        Conditions conditions = new Conditions(playerPredicate, speedtype, minimumSpeed, maximumSpeed, locomotion);
        return conditions;
    }

    protected void trigger(ServerPlayerEntity player, Speedtype speedtype, double minSpeed, double maxSpeed){
        trigger(player, conditions -> {
            return conditions.requirementsMet(player, speedtype, minSpeed, maxSpeed);
        });
    }
}
