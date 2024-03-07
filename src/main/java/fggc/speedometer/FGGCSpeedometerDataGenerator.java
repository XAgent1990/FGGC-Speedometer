package fggc.speedometer;

import java.util.function.Consumer;

import fggc.speedometer.FGGCSpeedometer.Speedtype;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FGGCSpeedometerDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(AdvancementProvider::new);
    }

    static class AdvancementProvider extends FabricAdvancementProvider {
        protected AdvancementProvider(FabricDataGenerator fabricDataGenerator) {
            super(fabricDataGenerator);
        }

        @Override
        public void generateAdvancement(Consumer<Advancement> consumer) {
            String speedName = "reached_speed_";
            String speedDir = "/speed";

            // Befehl das erste mal eingeben
            Advancement rootAdvancement = Advancement.Builder.create()
                    .display(
                            FGGCSpeedometer.FGGC_ITEM, // The display icon
                            Text.literal("So schnell bin ich also!"), // The title
                            Text.literal("Nutze den /tacho Befehl um die Geschwindigkeitsanzeige zu aktivieren"), // The description
                            new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                            AdvancementFrame.TASK, // Options: TASK, CHALLENGE, GOAL
                            true, // Show toast top right
                            true, // Announce to chat
                            false // Hidden in the advancement tab
                    )
                    // The first string used in criterion is the name referenced by other
                    // advancements when they want to have 'requirements'
                    .criterion("used_command", new FGGCCommandCriterion.Conditions(Extended.EMPTY))
                    .build(consumer, FGGCSpeedometer.MOD_ID + "/root");
            
            // 
            Advancement AT1 = Advancement.Builder.create().parent(rootAdvancement)
                    .display(
                            FGGCSpeedometer.FGGC_ITEM, // The display icon
                            Text.literal("AT1"), // The title
                            Text.literal("10"), // The description
                            new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                            AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                            true, // Show toast top right
                            true, // Announce to chat
                            false // Hidden in the advancement tab
                    )
                    // The first string used in criterion is the name referenced by other
                    // advancements when they want to have 'requirements'
                    .criterion(speedName + "t_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY, Speedtype.Total, 10d, 0d))
                    .build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/at1");
            
            Advancement AT2 = Advancement.Builder.create().parent(AT1)
                    .display(
                            FGGCSpeedometer.FGGC_ITEM, // The display icon
                            Text.literal("AT2"), // The title
                            Text.literal("20"), // The description
                            new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                            AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                            true, // Show toast top right
                            true, // Announce to chat
                            false // Hidden in the advancement tab
                    )
                    // The first string used in criterion is the name referenced by other
                    // advancements when they want to have 'requirements'
                    .criterion(speedName + "t_2", new FGGCSpeedCriterion.Conditions(Extended.EMPTY, Speedtype.Total, 20d, 0d))
                    .build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/at2");
            
            
            Advancement AH1 = Advancement.Builder.create().parent(rootAdvancement)
                    .display(
                            FGGCSpeedometer.FGGC_ITEM, // The display icon
                            Text.literal("AH1"), // The title
                            Text.literal("5"), // The description
                            new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                            AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                            true, // Show toast top right
                            true, // Announce to chat
                            false // Hidden in the advancement tab
                    )
                    // The first string used in criterion is the name referenced by other
                    // advancements when they want to have 'requirements'
                    .criterion(speedName + "h_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY, Speedtype.Horizontal, 5d, 0d))
                    .build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/ah1");
            
            Advancement AH2 = Advancement.Builder.create().parent(AH1)
                    .display(
                            FGGCSpeedometer.FGGC_ITEM, // The display icon
                            Text.literal("AH2"), // The title
                            Text.literal("8"), // The description
                            new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                            AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                            true, // Show toast top right
                            true, // Announce to chat
                            false // Hidden in the advancement tab
                    )
                    // The first string used in criterion is the name referenced by other
                    // advancements when they want to have 'requirements'
                    .criterion(speedName + "h_2", new FGGCSpeedCriterion.Conditions(Extended.EMPTY, Speedtype.Horizontal, 8d, 0d))
                    .build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/ah2");
            
                    Advancement AV1 = Advancement.Builder.create().parent(rootAdvancement)
                    .display(
                            FGGCSpeedometer.FGGC_ITEM, // The display icon
                            Text.literal("AV1"), // The title
                            Text.literal("40"), // The description
                            new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                            AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                            true, // Show toast top right
                            true, // Announce to chat
                            false // Hidden in the advancement tab
                    )
                    // The first string used in criterion is the name referenced by other
                    // advancements when they want to have 'requirements'
                    .criterion(speedName + "v_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY, Speedtype.Vertical, 40d, 0d))
                    .build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/av1");
        }
    }
}
