package fggc.speedometer;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import fggc.speedometer.FGGCSpeedometer.Speedtype;

public class FGGCSpeedCriterion extends AbstractCriterion<FGGCSpeedCriterion.Conditions> {
    static final Identifier ID = new Identifier("fggc-speedometer","speed");

    public static class Conditions extends AbstractCriterionConditions {

        double minimumSpeed;
        double minimumSpeedH;
        double minimumSpeedV;
        double maximumSpeed;
        Speedtype speedtype;

        public Conditions(Extended entity, Speedtype speedtype, double minimumSpeed, double maximumSpeed) {
            super(FGGCSpeedCriterion.ID, entity);
            this.minimumSpeed = this.minimumSpeedH = this.minimumSpeedV = 0d;
            this.speedtype = speedtype;
            this.maximumSpeed = maximumSpeed;
            switch (speedtype) {
                case Total:
                    this.minimumSpeed = minimumSpeed;
                    break;
                case Horizontal:
                    this.minimumSpeedH = minimumSpeed;
                    break;
                case Vertical:
                    this.minimumSpeedV = minimumSpeed;
                    break;
                default:
                    break;
            }
        }
 
        boolean requirementsMet(Speedtype speedtype, double speed) {
            if(this.speedtype != speedtype) return false;
            switch (this.speedtype) {
                case Total:
                    return speed >= minimumSpeed && maximumSpeed > 0 ? speed <= maximumSpeed : true;
                case Horizontal:
                    return speed >= minimumSpeedH && maximumSpeed > 0 ? speed <= maximumSpeed : true;
                case Vertical:
                    return speed >= minimumSpeedV && maximumSpeed > 0 ? speed <= maximumSpeed : true;
                default:
                    return false;
            }
        }
        
        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject json = super.toJson(predicateSerializer);
            json.addProperty("speed", this.minimumSpeed);
            json.addProperty("horizontal_speed", this.minimumSpeedH);
            json.addProperty("vertical_speed", this.minimumSpeedV);
            json.addProperty("maxmium_speed", this.maximumSpeed);
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
        Speedtype speedtype = Speedtype.Total;
        double minimumSpeed = obj.get("speed").getAsDouble();
        double minimumSpeedH = obj.get("horizontal_speed").getAsDouble();
        double minimumSpeedV = obj.get("vertical_speed").getAsDouble();
        double maximumSpeed = obj.get("maximum_speed").getAsDouble();
        double minSpeed = 0d;
        if(minimumSpeed > 0d){
            speedtype = Speedtype.Total;
            minSpeed = minimumSpeed;
        } else if(minimumSpeedH > 0d){
            speedtype = Speedtype.Horizontal;
            minSpeed = minimumSpeedH;
        } else if(minimumSpeedV > 0d){
            speedtype = Speedtype.Vertical;
            minSpeed = minimumSpeedV;
        }
        Conditions conditions = new Conditions(playerPredicate, speedtype, minSpeed, maximumSpeed);
        return conditions;
    }

    protected void trigger(ServerPlayerEntity player, Speedtype speedtype, double speed){
        trigger(player, conditions -> {
            return conditions.requirementsMet(speedtype, speed);
        });
    }
}
