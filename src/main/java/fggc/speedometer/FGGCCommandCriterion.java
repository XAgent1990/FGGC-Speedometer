package fggc.speedometer;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class FGGCCommandCriterion extends AbstractCriterion<FGGCCommandCriterion.Conditions> {
    static final Identifier ID = new Identifier("fggc-speedometer","command");

    public static class Conditions extends AbstractCriterionConditions {
        public Conditions(Extended entity) {
            super(FGGCCommandCriterion.ID, entity);
        }
 
        boolean requirementsMet() {
            return true;
        }
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Extended playerPredicate,
            AdvancementEntityPredicateDeserializer predicateDeserializer) {
        Conditions conditions = new Conditions(playerPredicate);
        return conditions;
    }

    protected void trigger(ServerPlayerEntity player){
        trigger(player, conditions -> {
            return conditions.requirementsMet();
        });
    }
}
