package fggc.speedometer;

import java.util.function.Consumer;

import fggc.speedometer.FGGCSpeedometer.Locomotion;
import fggc.speedometer.FGGCSpeedometer.Speedtype;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
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
            NbtCompound nbt = new NbtCompound();
            NbtCompound nbt1 = new NbtCompound();
            NbtList nbtlist = new NbtList();
            ItemStack FGGC_Banner = new ItemStack(Items.RED_BANNER);
            nbt.putByte("Color", (byte)15);
            nbt.putString("Pattern", "gra");
            nbtlist.add(nbt);
            nbtlist.add(nbt);
            nbtlist.add(nbt);
            nbt = new NbtCompound();
            nbt.putByte("Color", (byte)8);
            nbt.putString("Pattern", "sku");
            nbtlist.add(nbt);
            nbt = new NbtCompound();
            nbt.put("Patterns",  nbtlist);
            nbt1.put("BlockEntityTag", nbt);
            FGGC_Banner.setNbt(nbt1);

            Advancement rootAdvancement = Advancement.Builder.create()
            .display(
                FGGC_Banner, // The display icon
                Text.literal("So schnell bin ich also!"), // The title
                Text.literal("Nutze den /tacho Befehl um die Geschwindigkeitsanzeige zu aktivieren"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.TASK, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion("used_command", new FGGCCommandCriterion.Conditions(Extended.EMPTY))
            .build(consumer, FGGCSpeedometer.MOD_ID + "/root");
            
            Advancement boat_1 = Advancement.Builder.create().parent(rootAdvancement)
            .display(
                Items.OAK_BOAT, // The display icon
                Text.literal("Volle Kraft voraus!"), // The title
                Text.literal("Fahre ein Boot bei voller Geschwindigkeit"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "boat_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Horizontal, 
                7.9d, 
                8.1d, 
                Locomotion.Boat
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/boat_1");
            
            Advancement boat_s_1 = Advancement.Builder.create().parent(boat_1)
            .display(
                Items.OAK_BOAT, // The display icon
                Text.literal("Chuck Norris wäre stolz"), // The title
                Text.literal("Fahre ein Boot bei voller Geschwindigkeit... durchs Land!"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                true // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "boat_s_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Horizontal, 
                2d, 
                2d, 
                Locomotion.Boat
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/boat_s_1");
            
            Advancement boat_2 = Advancement.Builder.create().parent(boat_1)
            .display(
                Items.OAK_BOAT, // The display icon
                Text.literal("Go-Kart in Minecraft?!"), // The title
                Text.literal("Fahre ein Boot bei voller Geschwindigkeit auf Eis"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "boat_2", new FGGCSpeedCriterion.Conditions(Extended.EMPTY, 
                Speedtype.Horizontal, 
                38d, 
                42d, 
                Locomotion.Boat
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/boat_2");
            
            Advancement boat_3 = Advancement.Builder.create().parent(boat_2)
            .display(
                Items.OAK_BOAT, // The display icon
                Text.literal("Warpspeed!"), // The title
                Text.literal("Erreiche Serverleistungstestend-hohe Geschwindigkeiten auf Blaueis"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.CHALLENGE, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                true, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "boat_3", new FGGCSpeedCriterion.Conditions(Extended.EMPTY, 
                Speedtype.Horizontal, 
                60d, 
                0d, 
                Locomotion.Boat
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/boat_3");
            
                
            
            Advancement cart_1 = Advancement.Builder.create().parent(rootAdvancement)
            .display(
                Items.MINECART, // The display icon
                Text.literal("Volle Fahrt voraus!"), // The title
                Text.literal("Fahre eine Lore bei voller Geschwindigkeit"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "cart_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Horizontal, 
                7.9d, 
                8.1d, 
                Locomotion.Minecart
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/cart_1");
        
            Advancement cart_s_1 = Advancement.Builder.create().parent(cart_1)
            .display(
                Items.MINECART, // The display icon
                Text.literal("Schräge Sache"), // The title
                Text.literal("Fahre eine Lore bei voller Geschwindigkeit... auf einer diagonale?"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.CHALLENGE, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                true // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "cart_s_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Horizontal, 
                11.2d, 
                11.4d, 
                Locomotion.Minecart
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/cart_s_1");



            Advancement swim_1 = Advancement.Builder.create().parent(rootAdvancement)
            .display(
                Items.WATER_BUCKET, // The display icon
                Text.literal("Seepferdchen!"), // The title
                Text.literal("Schwimme bei voller Geschwindigkeit"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "swim_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Horizontal, 
                3.92d, 
                3.92d, 
                Locomotion.Swimming
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/swim_1");
            
            Advancement swim_s_1 = Advancement.Builder.create().parent(swim_1)
            .display(
                Items.WATER_BUCKET, // The display icon
                Text.literal("Schneller ist schneller..."), // The title
                Text.literal("Schwimme in einer diagonalen, um einen Meter Vorsprung in nur 12,5 Sekunden zu erlangen!"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.TASK, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                true // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "swim_s_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Horizontal, 
                4d, 
                4d, 
                Locomotion.Swimming
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/swim_s_1");
            
            Advancement swim_2_1 = Advancement.Builder.create().parent(swim_1)
            .display(
                Items.WATER_BUCKET, // The display icon
                Text.literal("Freischwimmer!"), // The title
                Text.literal("Schwimme bei voller Geschwindigkeit mit passender Schwimmausrüstung"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "swim_2_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY, 
                Speedtype.Horizontal, 
                5.31d, 
                5.31d, 
                Locomotion.Swimming
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/swim_2_1");
            
            Advancement swim_s_2 = Advancement.Builder.create().parent(swim_2_1)
            .display(
                Items.WATER_BUCKET, // The display icon
                Text.literal("Hilfsmittelschwimmer..."), // The title
                Text.literal("Nutze den Boden um mit deiner Ausrüstung noch schneller zu schwimmen"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.TASK, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                true // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "swim_s_2", new FGGCSpeedCriterion.Conditions(Extended.EMPTY, 
                Speedtype.Horizontal, 
                5.61d, 
                5.61d, 
                Locomotion.Swimming
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/swim_s_2");
            
            Advancement swim_2_2 = Advancement.Builder.create().parent(swim_1)
            .display(
                Items.WATER_BUCKET, // The display icon
                Text.literal("Wie ein Delfin!"), // The title
                Text.literal("Schwimme bei voller Geschwindigkeit mit einem Delfin"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "swim_2_2", new FGGCSpeedCriterion.Conditions(Extended.EMPTY, 
                Speedtype.Horizontal, 
                9.8d, 
                9.8d, 
                Locomotion.Swimming
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/swim_2_2");
            
            Advancement swim_3 = Advancement.Builder.create().parent(swim_2_1)
            .display(
                Items.WATER_BUCKET, // The display icon
                Text.literal("Aquaman!"), // The title
                Text.literal("Schwimme bei voller Geschwindigkeit mit passender Schwimmausrüstung und einem Delfin"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "swim_3", new FGGCSpeedCriterion.Conditions(Extended.EMPTY, 
                Speedtype.Horizontal, 
                30d, 
                37d, 
                Locomotion.Swimming
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/swim_3");
            
            Advancement swim_s_3 = Advancement.Builder.create().parent(swim_3)
            .display(
                Items.WATER_BUCKET, // The display icon
                Text.literal("Menschlicher Torpedo..."), // The title
                Text.literal("Nutze den Boden um übermenschlich schnell zu schwimmen"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.CHALLENGE, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                true // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "swim_s_3", new FGGCSpeedCriterion.Conditions(Extended.EMPTY, 
                Speedtype.Horizontal, 
                60d, 
                65d, 
                Locomotion.Swimming
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/swim_s_3");



            Advancement foot_1 = Advancement.Builder.create().parent(rootAdvancement)
            .display(
                Items.LEATHER_BOOTS, // The display icon
                Text.literal("Flash! Ahhhhh!"), // The title
                Text.literal("Renne, so schnell du kannst!"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "foot_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Total, 
                5.61d, 
                5.73d, 
                Locomotion.OnFoot
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/foot_1");
            
            Advancement foot_s_1 = Advancement.Builder.create().parent(foot_1)
            .display(
                Items.LEATHER_BOOTS, // The display icon
                Text.literal("Blitzschleicher!"), // The title
                Text.literal("Schleiche deutlich schneller, durch Diagonalität!"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                true // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "foot_s_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Horizontal, 
                1.8d, 
                1.8d, 
                Locomotion.OnFoot
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/foot_s_1");
            
            Advancement foot_2 = Advancement.Builder.create().parent(foot_1)
            .display(
                Items.LEATHER_BOOTS, // The display icon
                Text.literal("Usain Bolt!"), // The title
                Text.literal("Der schnellte Mensch der Welt! Renne mit circa 10,44m/s"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.CHALLENGE, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                true, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "foot_2", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Horizontal, 
                10.4d, 
                10.5d, 
                Locomotion.OnFoot
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/foot_2");



            Advancement fall_1 = Advancement.Builder.create().parent(rootAdvancement)
            .display(
                Items.ANVIL, // The display icon
                Text.literal("Schwerkraft ist ein übles Ding"), // The title
                Text.literal("Erreiche eine schmerzhafte Fallgeschwindigkeit"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "fall_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Vertical, 
                13d, 
                100d, 
                Locomotion.Falling
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/fall_1");

            Advancement fall_2 = Advancement.Builder.create().parent(fall_1)
            .display(
                Items.ANVIL, // The display icon
                Text.literal("Das wird schmerzhaft..."), // The title
                Text.literal("Erreiche eine potentiell tödliche Fallgeschwindigkeit"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "fall_2", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Vertical, 
                32d, 
                100d, 
                Locomotion.Falling
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/fall_2");

            Advancement fall_3 = Advancement.Builder.create().parent(fall_2)
            .display(
                Items.ANVIL, // The display icon
                Text.literal("Terminal Velocity!"), // The title
                Text.literal("Erreiche maximale Geschwindigkeit im freien Fall!"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.CHALLENGE, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                true, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "fall_3", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Vertical, 
                78.395d, 
                100d, 
                Locomotion.Falling
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/fall_3");



            Advancement fly_1 = Advancement.Builder.create().parent(rootAdvancement)
            .display(
                Items.ELYTRA, // The display icon
                Text.literal("Raketenantrieb"), // The title
                Text.literal("Fliege so schnell wie eine Rakete (30+m/s)"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "fly_1", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Total, 
                30d, 
                0d, 
                Locomotion.Elytra
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/fly_1");

            Advancement fly_2 = Advancement.Builder.create().parent(fly_1)
            .display(
                Items.ELYTRA, // The display icon
                Text.literal("Dreizackantrieb"), // The title
                Text.literal("Erreiche Windresistenzbrechende Geschwindigkeiten (100+m/s)"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.GOAL, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                false, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "fly_2", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Vertical, 
                100d, 
                0d, 
                Locomotion.Elytra
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/fly_2");

            Advancement fly_3 = Advancement.Builder.create().parent(fly_2)
            .display(
                Items.ELYTRA, // The display icon
                Text.literal("Überschall!"), // The title
                Text.literal("Breche die Schallmauer! (343+m/s)"), // The description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
                AdvancementFrame.CHALLENGE, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                true, // Announce to chat
                false // Hidden in the advancement tab
            )
            // The first string used in criterion is the name referenced by other
            // advancements when they want to have 'requirements'
            .criterion(speedName + "fly_3", new FGGCSpeedCriterion.Conditions(Extended.EMPTY,
                Speedtype.Vertical, 
                343d, 
                0d, 
                Locomotion.Elytra
            )).build(consumer, FGGCSpeedometer.MOD_ID + speedDir + "/fly_3");
        }
    }
}
