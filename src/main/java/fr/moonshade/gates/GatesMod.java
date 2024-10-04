package fr.moonshade.gates;

import fr.moonshade.gates.blocks.CardGetter;
import fr.moonshade.gates.blocks.ModBlock;
import fr.moonshade.gates.doors.*;
import fr.moonshade.gates.gui.*;
import fr.moonshade.gates.items.*;
import fr.moonshade.gates.setup.*;
import fr.moonshade.gates.tileentity.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.logging.Logger;

@Mod(GatesMod.MOD_ID)
public class GatesMod {

    public static IProxy proxy = DistExecutor.runForDist(()-> ClientProxy::new,()-> ServerProxy::new);

    public static ModSetup setup = new ModSetup();


    public static final String MOD_ID = "gates";

    public static final Logger logger = Logger.getLogger(MOD_ID);

    public GatesMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::preInit);

        MinecraftForge.EVENT_BUS.register(this);

    }

    public void preInit(FMLCommonSetupEvent evt) {


    }

    private void setup(final FMLCommonSetupEvent event) {

        proxy.init();
        setup.init();

    }



    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)

    public static class RegistryEvents {

        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            blockRegistryEvent.getRegistry().register(new TollGate());
            blockRegistryEvent.getRegistry().register(new RedstoneTollGate());
            blockRegistryEvent.getRegistry().register(new TurnStile());
            blockRegistryEvent.getRegistry().register(new RedstoneTurnStile());
            blockRegistryEvent.getRegistry().register(new CardGetter());
            WindowDoor windowDoor = new WindowDoor();
            blockRegistryEvent.getRegistry().register(windowDoor);
            if (FMLEnvironment.dist == Dist.CLIENT){
                ItemBlockRenderTypes.setRenderLayer(windowDoor, RenderType.cutoutMipped());
            }
            DoorRegister.registerBlocks(blockRegistryEvent);
        }


        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> blockRegistryEvent) {
            Item.Properties properties = new Item.Properties().tab(ModSetup.itemGroup);
            blockRegistryEvent.getRegistry().register(new TollGateItem(ModBlock.TOLL_GATE));
            blockRegistryEvent.getRegistry().register(ModItem.TOLL_GATE_KEY);
            blockRegistryEvent.getRegistry().register(ModItem.CARD_KEY);
            blockRegistryEvent.getRegistry().register(new TurnStileItem(ModBlock.TURN_STILE));
            blockRegistryEvent.getRegistry().register(ModItem.TURN_STILE_KEY);
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.CARD_GETTER,properties).setRegistryName("card_getter"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.WINDOW_DOOR,properties).setRegistryName("window_door"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.REDSTONE_TOLL_GATE, properties).setRegistryName("redstone_toll_gate"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.REDSTONE_TURN_STILE, properties).setRegistryName("redstone_turn_stile"));
            DoorRegister.registerItems(blockRegistryEvent);
        }

        @SubscribeEvent
        public static void onTileEntityRegistry(final RegistryEvent.Register<BlockEntityType<?>> event){
            event.getRegistry().register(
                    BlockEntityType.Builder.of(GarageTileEntity::new,
                            DoorRegister.ANDESITE_GARAGE,
                            DoorRegister.DIORITE_GARAGE,
                            DoorRegister.GRANITE_GARAGE,
                            DoorRegister.IRON_GARAGE,
                            DoorRegister.STONE_GARAGE,
                            DoorRegister.STONE_BRICKS_GARAGE,
                            DoorRegister.BLACK_GARAGE,
                            DoorRegister.BLUE_GARAGE,
                            DoorRegister.BROWN_GARAGE,
                            DoorRegister.CYAN_GARAGE,
                            DoorRegister.GRAY_GARAGE,
                            DoorRegister.GREEN_GARAGE,
                            DoorRegister.L_BLUE_GARAGE,
                            DoorRegister.L_GRAY_GARAGE,
                            DoorRegister.LIME_GARAGE,
                            DoorRegister.MAGENTA_GARAGE,
                            DoorRegister.ORANGE_GARAGE,
                            DoorRegister.PINK_GARAGE,
                            DoorRegister.PURPLE_GARAGE,
                            DoorRegister.RED_GARAGE,
                            DoorRegister.WHITE_GARAGE,
                            DoorRegister.YELLOW_GARAGE
                            )
                            .build(null)
                            .setRegistryName("garage_door"));
            event.getRegistry().register(BlockEntityType.Builder.of(TollGateTileEntity::new,
                    ModBlock.TOLL_GATE)
                    .build(null)
                    .setRegistryName("toll_gate"));
            event.getRegistry().register(BlockEntityType.Builder.of(RedstoneTollGateTileEntity::new,
                    ModBlock.REDSTONE_TOLL_GATE)
                    .build(null)
                    .setRegistryName("redstone_toll_gate"));
            event.getRegistry().register(BlockEntityType.Builder.of(TurnStileTileEntity::new,
                    ModBlock.TURN_STILE)
                    .build(null)
                    .setRegistryName("turn_stile"));
            event.getRegistry().register(BlockEntityType.Builder.of(RedstoneTurnStileTileEntity::new,
                    ModBlock.REDSTONE_TURN_STILE)
                    .build(null)
                    .setRegistryName("redstone_turn_stile"));
            event.getRegistry().register(BlockEntityType.Builder.of(CardGetterTileEntity::new,
                    ModBlock.CARD_GETTER)
                    .build(null)
                    .setRegistryName("card_getter"));
            event.getRegistry().register(BlockEntityType.Builder.of(WindowDoorTileEntity::new,
                    ModBlock.WINDOW_DOOR)
                    .build(null)
                    .setRegistryName("window_door"));
            event.getRegistry().register(BlockEntityType.Builder.of(DrawBridgeTileEntity::new,
                    DoorRegister.ACACIA_DRAW_BRIDGE,
                    DoorRegister.DARK_OAK_DRAW_BRIDGE,
                    DoorRegister.BIRCH_DRAW_BRIDGE,
                    DoorRegister.OAK_DRAW_BRIDGE,
                    DoorRegister.SPRUCE_DRAW_BRIDGE,
                    DoorRegister.JUNGLE_DRAW_BRIDGE)
                    .build(null)
                    .setRegistryName("draw_bridge"));
        }

        @SubscribeEvent
        public static void onContainerRegistry(final RegistryEvent.Register<MenuType<?>> event){
            event.getRegistry().register(IForgeMenuType.create(((windowId, inv, data) -> {
                BlockPos pos =data.readBlockPos();
                return new TGUserContainer(windowId,
                        GatesMod.proxy.getClientWorld(),
                        pos,
                        inv,
                        GatesMod.proxy.getClientPlayer());
            })).setRegistryName("toll_gate"));
            event.getRegistry().register(IForgeMenuType.create(((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                return new TGTechContainer(windowId,
                        GatesMod.proxy.getClientWorld(),
                        pos,
                        inv,
                        GatesMod.proxy.getClientPlayer());
            })).setRegistryName("toll_gate_tech"));

            event.getRegistry().register(IForgeMenuType.create(((windowId, inv, data) -> {
                BlockPos pos =data.readBlockPos();
                return new TSContainer(windowId,
                        GatesMod.proxy.getClientWorld(),
                        pos,
                        inv,
                        GatesMod.proxy.getClientPlayer());
            })).setRegistryName("turn_stile"));

            event.getRegistry().register(IForgeMenuType.create(((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                return new CardGetterContainer(windowId,
                        GatesMod.proxy.getClientWorld(),
                        pos,
                        inv,
                        GatesMod.proxy.getClientPlayer());
            })).setRegistryName("card_getter"));

            event.getRegistry().register(IForgeMenuType.create(((windowId, inv, data) -> {
                BlockPos pos =data.readBlockPos();
                return new CardGetterChoiceContainer(windowId,
                        GatesMod.proxy.getClientWorld(),
                        pos,
                        GatesMod.proxy.getClientPlayer());
            })).setRegistryName("card_getter_choice"));
        }

        @SubscribeEvent
        public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
            ModSound.registerSounds(event);
        }

    }
}


