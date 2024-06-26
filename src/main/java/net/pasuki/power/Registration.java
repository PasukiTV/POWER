package net.pasuki.power;

import net.pasuki.power.blocks.*;
import net.pasuki.power.cables.blocks.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("DataFlowIssue")
public class Registration {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Power.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Power.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Power.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Power.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Power.MODID);

    public static final RegistryObject<GeneratorBlock> GENERATOR_BLOCK = BLOCKS.register("generator_block", GeneratorBlock::new);
    public static final RegistryObject<Item> GENERATOR_BLOCK_ITEM = ITEMS.register("generator_block", () -> new BlockItem(GENERATOR_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<GeneratorBlockEntity>> GENERATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("generator_block",
            () -> BlockEntityType.Builder.of(GeneratorBlockEntity::new, GENERATOR_BLOCK.get()).build(null));
    public static final RegistryObject<MenuType<GeneratorContainer>> GENERATOR_CONTAINER = MENU_TYPES.register("generator_block",
            () -> IForgeMenuType.create((windowId, inv, data) -> new GeneratorContainer(windowId, inv.player, data.readBlockPos())));

    public static final RegistryObject<ChargerBlock> CHARGER_BLOCK = BLOCKS.register("charger_block", ChargerBlock::new);
    public static final RegistryObject<Item> CHARGER_BLOCK_ITEM = ITEMS.register("charger_block", () -> new BlockItem(CHARGER_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<ChargerBlockEntity>> CHARGER_BLOCK_ENTITY = BLOCK_ENTITIES.register("charger_block",
            () -> BlockEntityType.Builder.of(ChargerBlockEntity::new, CHARGER_BLOCK.get()).build(null));

    public static final RegistryObject<SolarPanelBlock> SOLAR_PANEL_BLOCK = BLOCKS.register("solar_panel_block", SolarPanelBlock::new);
    public static final RegistryObject<Item> SOLAR_PANEL_BLOCK_ITEM = ITEMS.register("solar_panel_block", () -> new BlockItem(SOLAR_PANEL_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL_BLOCK_ENTITY = BLOCK_ENTITIES.register("solar_panel_block",
            () -> BlockEntityType.Builder.of(SolarPanelBlockEntity::new, SOLAR_PANEL_BLOCK.get()).build(null));

    public static final RegistryObject<BatteryBlock> BATTERY_BLOCK = BLOCKS.register("battery_block", BatteryBlock::new);
    public static final RegistryObject<Item> BATTERY_BLOCK_ITEM = ITEMS.register("battery_block", () -> new BlockItem(BATTERY_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<BatteryBlockEntity>> BATTERY_BLOCK_ENTITY = BLOCK_ENTITIES.register("battery_block",
            () -> BlockEntityType.Builder.of(BatteryBlockEntity::new, BATTERY_BLOCK.get()).build(null));
    public static final RegistryObject<MenuType<BatteryContainer>> BATTERY_CONTAINER = MENU_TYPES.register("battery_block",
            () -> IForgeMenuType.create((windowId, inv, data) -> new BatteryContainer(windowId, inv.player, data.readBlockPos())));


    public static final RegistryObject<CableBlock> CABLE_BLOCK = BLOCKS.register("cable", CableBlock::new);
    public static final RegistryObject<Item> CABLE_BLOCK_ITEM = ITEMS.register("cable", () -> new BlockItem(CABLE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<CableBlockEntity>> CABLE_BLOCK_ENTITY = BLOCK_ENTITIES.register("cable",
            () -> BlockEntityType.Builder.of(CableBlockEntity::new, CABLE_BLOCK.get()).build(null));

    public static final RegistryObject<FacadeBlock> FACADE_BLOCK = BLOCKS.register("facade", FacadeBlock::new);
    public static final RegistryObject<Item> FACADE_BLOCK_ITEM = ITEMS.register("facade", () -> new FacadeBlockItem(FACADE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<FacadeBlockEntity>> FACADE_BLOCK_ENTITY = BLOCK_ENTITIES.register("facade",
            () -> BlockEntityType.Builder.of(FacadeBlockEntity::new, FACADE_BLOCK.get()).build(null));

    public static RegistryObject<CreativeModeTab> TAB = TABS.register("tutpower", () -> CreativeModeTab.builder()
            .title(Component.translatable("tab.tutpower"))
            .icon(() -> new ItemStack(GENERATOR_BLOCK.get()))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((featureFlags, output) -> {
                output.accept(GENERATOR_BLOCK.get());
                output.accept(CHARGER_BLOCK.get());
                output.accept(SOLAR_PANEL_BLOCK.get());
                output.accept(BATTERY_BLOCK.get());
                output.accept(CABLE_BLOCK.get());
                output.accept(FACADE_BLOCK.get());
            })
            .build());

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        TABS.register(modEventBus);
    }

}
