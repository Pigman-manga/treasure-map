package net.apothem.treasuremap;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapDecorationType;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
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
				.then(argument("x", IntegerArgumentType.integer())
					.then(argument("z", IntegerArgumentType.integer())
						.then(argument("icon", StringArgumentType.word())
							.executes(this::createTreasureMap)))));
        });
	}

	public int createTreasureMap(CommandContext<ServerCommandSource> context){
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayer();

		ItemStack handItem = player.getMainHandStack();
		if (!handItem.isOf(Items.MAP))
			return 0;

		int x = IntegerArgumentType.getInteger(context, "x");
		int z = IntegerArgumentType.getInteger(context, "z");
		String iconId = StringArgumentType.getString(context, "icon");
		RegistryEntry<MapDecorationType> decorationType = getDecorationType(iconId);

		ItemStack map = FilledMapItem.createMap(player.getWorld(), x, z, (byte)1, true, true);
		FilledMapItem.fillExplorationMap(source.getWorld(), map);

		ComponentMap componentMap = map.getComponents();
		MapDecorationsComponent mapDecorationsComponent = componentMap.get(DataComponentTypes.MAP_DECORATIONS);
		if (mapDecorationsComponent == null) {
			mapDecorationsComponent = MapDecorationsComponent.DEFAULT;
		}

		mapDecorationsComponent = mapDecorationsComponent.with(
			"custom_marker",
			new MapDecorationsComponent.Decoration(decorationType, x, z, 0.f)
		);
		map.set(DataComponentTypes.MAP_DECORATIONS, mapDecorationsComponent);
		map.set(DataComponentTypes.ITEM_NAME, Text.translatable("filled_map.buried_treasure"));

		player.setStackInHand(Hand.MAIN_HAND, map);
		return 0;
	}

	private RegistryEntry<MapDecorationType> getDecorationType(String iconId) {
		return switch (iconId.toLowerCase()) {
			case "mansion" -> MapDecorationTypes.MANSION;
			case "monument" -> MapDecorationTypes.MONUMENT;
			case "treasure", "red_x" -> MapDecorationTypes.RED_X;
			case "red_marker" -> MapDecorationTypes.RED_MARKER;
			case "white_banner", "banner_white" -> MapDecorationTypes.WHITE_BANNER;
			case "igloo" -> MapDecorationTypes.IGLOO;
			case "jungle_temple" -> MapDecorationTypes.JUNGLE_TEMPLE;
			default -> MapDecorationTypes.RED_X;
		};
	}
}
