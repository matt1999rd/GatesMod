package fr.mattmouss.gates;

import fr.mattmouss.gates.blocks.CardGetter;
import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.doors.*;
import fr.mattmouss.gates.gui.*;
import fr.mattmouss.gates.items.*;
import fr.mattmouss.gates.setup.ClientProxy;
import fr.mattmouss.gates.setup.IProxy;
import fr.mattmouss.gates.setup.ModSetup;
import fr.mattmouss.gates.setup.ServerProxy;
import fr.mattmouss.gates.tileentity.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.logging.Logger;

@Mod(GatesMod.MODID)
public class GatesMod {

    public static IProxy proxy = DistExecutor.runForDist(()->()->new ClientProxy(),()->()->new ServerProxy());

    public static ModSetup setup = new ModSetup();


    public static final String MODID = "gates";

    public static final Logger logger = Logger.getLogger(MODID);

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
                RenderTypeLookup.setRenderLayer(windowDoor, RenderType.cutoutMipped());
            }
        }


        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> blockRegistryEvent) {
            Item.Properties properties = new Item.Properties().tab(ModSetup.itemGroup);
            blockRegistryEvent.getRegistry().register(new TollGateItem(ModBlock.TOLL_GATE));
            blockRegistryEvent.getRegistry().register(new TollKeyItem());
            blockRegistryEvent.getRegistry().register(new CardKeyItem());
            blockRegistryEvent.getRegistry().register(new TurnStileItem(ModBlock.TURN_STILE));
            blockRegistryEvent.getRegistry().register(new TurnStileKeyItem());
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.CARD_GETTER,properties).setRegistryName("card_getter"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.WINDOW_DOOR,properties).setRegistryName("window_door"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.RTOLL_GATE, properties).setRegistryName("redstone_toll_gate"));
            blockRegistryEvent.getRegistry().register(new BlockItem(ModBlock.RTURN_STILE, properties).setRegistryName("redstone_turn_stile"));
        }

        @SubscribeEvent
        public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event){
            event.getRegistry().register(
                    TileEntityType.Builder.of(GarageTileEntity::new,
                            DoorRegister.ANDESITE_GARAGE,
                            DoorRegister.DIORITE_GARAGE,
                            DoorRegister.GRANITE_GARAGE,
                            DoorRegister.IRON_GARAGE,
                            DoorRegister.STONE_GARAGE,
                            DoorRegister.STONEBRICKS_GARAGE,
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
            event.getRegistry().register(TileEntityType.Builder.of(TollGateTileEntity::new,
                    ModBlock.TOLL_GATE)
                    .build(null)
                    .setRegistryName("toll_gate"));
            event.getRegistry().register(TileEntityType.Builder.of(RedstoneTollGateTileEntity::new,
                    ModBlock.RTOLL_GATE)
                    .build(null)
                    .setRegistryName("redstone_toll_gate"));
            event.getRegistry().register(TileEntityType.Builder.of(TurnStileTileEntity::new,
                    ModBlock.TURN_STILE)
                    .build(null)
                    .setRegistryName("turn_stile"));
            event.getRegistry().register(TileEntityType.Builder.of(RedstoneTurnStileTileEntity::new,
                    ModBlock.RTURN_STILE)
                    .build(null)
                    .setRegistryName("redstone_turn_stile"));
            event.getRegistry().register(TileEntityType.Builder.of(CardGetterTileEntity::new,
                    ModBlock.CARD_GETTER)
                    .build(null)
                    .setRegistryName("card_getter"));
            event.getRegistry().register(TileEntityType.Builder.of(WindowDoorTileEntity::new,
                    ModBlock.WINDOW_DOOR)
                    .build(null)
                    .setRegistryName("window_door"));
            event.getRegistry().register(TileEntityType.Builder.of(DrawBridgeTileEntity::new,
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
        public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> event){
            event.getRegistry().register(IForgeContainerType.create(((windowId, inv, data) -> {
                BlockPos pos =data.readBlockPos();
                return new TGUserContainer(windowId,
                        GatesMod.proxy.getClientWorld(),
                        pos,
                        inv,
                        GatesMod.proxy.getClientPlayer());
            })).setRegistryName("toll_gate"));
            event.getRegistry().register(IForgeContainerType.create(((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                return new TGTechContainer(windowId,
                        GatesMod.proxy.getClientWorld(),
                        pos,
                        inv,
                        GatesMod.proxy.getClientPlayer());
            })).setRegistryName("toll_gate_tech"));

            event.getRegistry().register(IForgeContainerType.create(((windowId, inv, data) -> {
                BlockPos pos =data.readBlockPos();
                return new TSContainer(windowId,
                        GatesMod.proxy.getClientWorld(),
                        pos,
                        inv,
                        GatesMod.proxy.getClientPlayer());
            })).setRegistryName("turn_stile"));

            event.getRegistry().register(IForgeContainerType.create(((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                return new CardGetterContainer(windowId,
                        GatesMod.proxy.getClientWorld(),
                        pos,
                        inv,
                        GatesMod.proxy.getClientPlayer());
            })).setRegistryName("card_getter"));

            event.getRegistry().register(IForgeContainerType.create(((windowId, inv, data) -> {
                BlockPos pos =data.readBlockPos();
                return new CardGetterChoiceContainer(windowId,
                        GatesMod.proxy.getClientWorld(),
                        pos,
                        GatesMod.proxy.getClientPlayer());
            })).setRegistryName("card_getter_choice"));
        }
    }
}


