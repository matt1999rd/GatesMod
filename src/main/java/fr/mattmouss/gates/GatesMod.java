package fr.mattmouss.gates;

import fr.mattmouss.gates.doors.*;
import fr.mattmouss.gates.gui.TGTechContainer;
import fr.mattmouss.gates.gui.TGUserContainer;
import fr.mattmouss.gates.items.TollGateItem;
import fr.mattmouss.gates.setup.ClientProxy;
import fr.mattmouss.gates.setup.IProxy;
import fr.mattmouss.gates.setup.ModSetup;
import fr.mattmouss.gates.setup.ServerProxy;
import fr.mattmouss.gates.tileentity.GarageTileEntity;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
        //SwitchStorageCapability.register();

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
        }


        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> blockRegistryEvent) {
            //Item.Properties properties = new Item.Properties().group(ModSetup.itemGroup);
            blockRegistryEvent.getRegistry().register(new TollGateItem(ModBlock.TOLL_GATE));
        }

        @SubscribeEvent
        public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event){
            event.getRegistry().register(
                    TileEntityType.Builder.create(GarageTileEntity::new,
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
            event.getRegistry().register(TileEntityType.Builder.create(TollGateTileEntity::new,
                    ModBlock.TOLL_GATE)
                    .build(null)
                    .setRegistryName("toll_gate"));
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
                        GatesMod.proxy.getClientPlayer());
            })).setRegistryName("toll_gate_tech"));
        }
    }
}


