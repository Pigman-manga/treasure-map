package net.apothem.treasuremap;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import com.mojang.brigadier.context.CommandContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationTypes;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class TreasureMap implements ModInitializer {
	public static final String MOD_ID = "treasuremap";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Treasure Map!");

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("CreateTreasureMap")
				.then(argument("coords", Vec3ArgumentType.vec3())
						.executes(this::createTreasureMap)));
        });
	}

	public int createTreasureMap(CommandContext<ServerCommandSource> context){
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayer();

		ItemStack handItem = player.getMainHandStack();
		if (!handItem.isOf(Items.MAP))
			return 0;

		Vec3d coords = Vec3ArgumentType.getVec3(context, "coords");

		ItemStack map = FilledMapItem.createMap(player.getWorld(), (int)coords.x, (int)coords.z, (byte)1, true, true);
		FilledMapItem.fillExplorationMap(source.getWorld(), map);

		ComponentMap componentMap = map.getComponents();
		MapDecorationsComponent mapDecorationsComponent = componentMap.get(DataComponentTypes.MAP_DECORATIONS);
		mapDecorationsComponent = mapDecorationsComponent.with("red_x", new MapDecorationsComponent.Decoration(MapDecorationTypes.RED_X, coords.x,  coords.z, 0.f));
		map.set(DataComponentTypes.MAP_DECORATIONS, mapDecorationsComponent);
		map.set(DataComponentTypes.ITEM_NAME, Text.translatable("filled_map.buried_treasure"));

		player.setStackInHand(Hand.MAIN_HAND, map);
		return 0;
	}
}