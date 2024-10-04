package fr.moonshade.gates.doors;

import fr.moonshade.gates.items.GarageDoorItem;
import fr.moonshade.gates.setup.ModSetup;
import fr.moonshade.gates.windows.WindowBlock;
import net.minecraft.world.level.block.Block;

import net.minecraft.world.level.block.DoorBlock;

//1.15+ import for windows
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.loading.FMLEnvironment;

public class DoorRegister {

    private static final List<DoorBlock> DOORS = new ArrayList<>();
    private static final List<Item> DOOR_ITEMS = new ArrayList<>();
    private static final List<LargeDoor> LARGE_DOORS = new ArrayList<>();
    private static final List<Item> LARGE_DOOR_ITEMS = new ArrayList<>();
    private static final List<WindowBlock> WINDOWS = new ArrayList<>();
    private static final List<Item> WINDOWS_ITEMS = new ArrayList<>();
    private static final List<GarageDoor> GARAGES = new ArrayList<>();
    private static final List<Item> GARAGE_ITEMS = new ArrayList<>();
    private static final List<GardenDoor> GARDEN_DOORS = new ArrayList<>();
    private static final List<Item> GARDEN_DOOR_ITEMS = new ArrayList<>();
    private static final List<DrawBridge> DRAW_BRIDGES = new ArrayList<>();
    private static final List<Item> DRAW_BRIDGE_ITEMS = new ArrayList<>();

    //registry of classic door block
    //base on stone type
    public static final DoorBlock ANDESITE_DOOR = register("andesite_door");
    public static final DoorBlock COBBLESTONE_DOOR = register("cobblestone_door");
    public static final DoorBlock DIORITE_DOOR = register("diorite_door");
    public static final DoorBlock GRANITE_DOOR = register("granite_door");
    public static final DoorBlock STONE_DOOR = register("stone_door");

    //based on color type
    //public static final DoorBlock DOOR = register("_door");
    public static final DoorBlock BLACK_DOOR = register("black_door");
    public static final DoorBlock BLUE_DOOR = register("blue_door");
    public static final DoorBlock BROWN_DOOR = register("brown_door");
    public static final DoorBlock CYAN_DOOR = register("cyan_door");
    public static final DoorBlock GRAY_DOOR = register("gray_door");
    public static final DoorBlock GREEN_DOOR = register("green_door");
    public static final DoorBlock L_BLUE_DOOR = register("light_blue_door");
    public static final DoorBlock L_GRAY_DOOR = register("light_gray_door");
    public static final DoorBlock LIME_DOOR = register("lime_door");
    public static final DoorBlock MAGENTA_DOOR = register("magenta_door");
    public static final DoorBlock ORANGE_DOOR = register("orange_door");
    public static final DoorBlock PINK_DOOR = register("pink_door");
    public static final DoorBlock PURPLE_DOOR = register("purple_door");
    public static final DoorBlock RED_DOOR = register("red_door");
    public static final DoorBlock WHITE_DOOR = register("white_door");
    public static final DoorBlock YELLOW_DOOR = register("yellow_door");

    //registry of classic windows
    //based on stone type
    public static final WindowBlock SB_WINDOWS = registerWindows("stone_bricks_window"); //ok
    public static final WindowBlock CB_WINDOWS = registerWindows("cobblestone_window"); //ok
    public static final WindowBlock STONE_WINDOWS = registerWindows("stone_window"); //ok
    public static final WindowBlock ANDESITE_WINDOWS = registerWindows("andesite_window"); //ok
    public static final WindowBlock GRANITE_WINDOWS = registerWindows("granite_window");  //ok
    public static final WindowBlock DIORITE_WINDOWS = registerWindows("diorite_window"); //ok
    public static final WindowBlock BRICK_WINDOWS = registerWindows("brick_window"); //ok

    //based on color type
    //public static final DoorBlock WINDOW = registerWindows("_window");
    public static final WindowBlock BLACK_WINDOW = registerWindows("black_window");
    public static final WindowBlock BLUE_WINDOW = registerWindows("blue_window");
    public static final WindowBlock BROWN_WINDOW = registerWindows("brown_window");
    public static final WindowBlock CYAN_WINDOW = registerWindows("cyan_window");
    public static final WindowBlock GRAY_WINDOW = registerWindows("gray_window");
    public static final WindowBlock GREEN_WINDOW = registerWindows("green_window");
    public static final WindowBlock L_BLUE_WINDOW = registerWindows("light_blue_window");
    public static final WindowBlock L_GRAY_WINDOW = registerWindows("light_gray_window");
    public static final WindowBlock LIME_WINDOW = registerWindows("lime_window");
    public static final WindowBlock MAGENTA_WINDOW = registerWindows("magenta_window");
    public static final WindowBlock ORANGE_WINDOW = registerWindows("orange_window");
    public static final WindowBlock PINK_WINDOW = registerWindows("pink_window");
    public static final WindowBlock PURPLE_WINDOW = registerWindows("purple_window");
    public static final WindowBlock RED_WINDOW = registerWindows("red_window");
    public static final WindowBlock WHITE_WINDOW = registerWindows("white_window");
    public static final WindowBlock YELLOW_WINDOW = registerWindows("yellow_window");


    //registry of garage door
    //based on stone type
    //public static final GarageDoor _GARAGE = registerGarage("_garage");
    public static final GarageDoor IRON_GARAGE = registerGarage("iron_garage");
    public static final GarageDoor ANDESITE_GARAGE = registerGarage("andesite_garage");
    public static final GarageDoor GRANITE_GARAGE = registerGarage("granite_garage");
    public static final GarageDoor DIORITE_GARAGE = registerGarage("diorite_garage");
    public static final GarageDoor STONE_GARAGE = registerGarage("stone_garage");
    public static final GarageDoor STONE_BRICKS_GARAGE = registerGarage("stone_bricks_garage");

    //based on color type
    //public static final GarageDoor _GARAGE = Garage_register("_garage");
    public static final GarageDoor BLACK_GARAGE = registerGarage("black_garage");
    public static final GarageDoor BLUE_GARAGE = registerGarage("blue_garage");
    public static final GarageDoor BROWN_GARAGE = registerGarage("brown_garage");
    public static final GarageDoor CYAN_GARAGE = registerGarage("cyan_garage");
    public static final GarageDoor GRAY_GARAGE = registerGarage("gray_garage");
    public static final GarageDoor GREEN_GARAGE = registerGarage("green_garage");
    public static final GarageDoor L_BLUE_GARAGE = registerGarage("light_blue_garage");
    public static final GarageDoor L_GRAY_GARAGE = registerGarage("light_gray_garage");
    public static final GarageDoor LIME_GARAGE = registerGarage("lime_garage");
    public static final GarageDoor MAGENTA_GARAGE = registerGarage("magenta_garage");
    public static final GarageDoor ORANGE_GARAGE = registerGarage("orange_garage");
    public static final GarageDoor PINK_GARAGE = registerGarage("pink_garage");
    public static final GarageDoor PURPLE_GARAGE = registerGarage("purple_garage");
    public static final GarageDoor RED_GARAGE = registerGarage("red_garage");
    public static final GarageDoor WHITE_GARAGE = registerGarage("white_garage");
    public static final GarageDoor YELLOW_GARAGE = registerGarage("yellow_garage");

    //registry of length 3 height 2 large door
    public static final LargeDoor HAUSSMANN_LARGE_DOOR = registerLargeDoor("haussmann_large_door",Material.STONE);
    public static final LargeDoor HAUSSMANN2_LARGE_DOOR = registerLargeDoor("haussmann2_large_door",Material.STONE);
    public static final LargeDoor IRON_LARGE_DOOR = registerLargeDoor("iron_large_door",Material.METAL);
    public static final LargeDoor OAK_LARGE_DOOR = registerLargeDoor("oak_large_door",Material.WOOD);
    public static final LargeDoor BIRCH_LARGE_DOOR = registerLargeDoor("birch_large_door",Material.WOOD);
    public static final LargeDoor DARK_OAK_LARGE_DOOR = registerLargeDoor("dark_oak_large_door",Material.WOOD);
    public static final LargeDoor ACACIA_LARGE_DOOR = registerLargeDoor("acacia_large_door",Material.WOOD);
    public static final LargeDoor SPRUCE_LARGE_DOOR = registerLargeDoor("spruce_large_door",Material.WOOD);
    public static final LargeDoor JUNGLE_LARGE_DOOR = registerLargeDoor("jungle_large_door",Material.WOOD);

    //registry of garden with grid entry door
    //based on color
    //public static final GardenDoor _GARDEN_DOOR = registerGardenDoor("_garden_door");
    public static final GardenDoor BLACK_GARDEN = registerGardenDoor("black_garden_door");
    public static final GardenDoor BLUE_GARDEN = registerGardenDoor("blue_garden_door");
    public static final GardenDoor BROWN_GARDEN = registerGardenDoor("brown_garden_door");
    public static final GardenDoor CYAN_GARDEN = registerGardenDoor("cyan_garden_door");
    public static final GardenDoor GRAY_GARDEN = registerGardenDoor("gray_garden_door");
    public static final GardenDoor GREEN_GARDEN = registerGardenDoor("green_garden_door");
    public static final GardenDoor L_BLUE_GARDEN = registerGardenDoor("light_blue_garden_door");
    public static final GardenDoor L_GRAY_GARDEN = registerGardenDoor("light_gray_garden_door");
    public static final GardenDoor LIME_GARDEN = registerGardenDoor("lime_garden_door");
    public static final GardenDoor MAGENTA_GARDEN = registerGardenDoor("magenta_garden_door");
    public static final GardenDoor ORANGE_GARDEN = registerGardenDoor("orange_garden_door");
    public static final GardenDoor PINK_GARDEN = registerGardenDoor("pink_garden_door");
    public static final GardenDoor PURPLE_GARDEN = registerGardenDoor("purple_garden_door");
    public static final GardenDoor RED_GARDEN = registerGardenDoor("red_garden_door");
    public static final GardenDoor WHITE_GARDEN = registerGardenDoor("white_garden_door");
    public static final GardenDoor YELLOW_GARDEN = registerGardenDoor("yellow_garden_door");

    //draw bridge for all wood type
    public static final DrawBridge OAK_DRAW_BRIDGE = registerDrawBridge("oak_draw_bridge");
    public static final DrawBridge DARK_OAK_DRAW_BRIDGE = registerDrawBridge("dark_oak_draw_bridge");
    public static final DrawBridge BIRCH_DRAW_BRIDGE = registerDrawBridge("birch_draw_bridge");
    public static final DrawBridge JUNGLE_DRAW_BRIDGE = registerDrawBridge("jungle_draw_bridge");
    public static final DrawBridge ACACIA_DRAW_BRIDGE = registerDrawBridge("acacia_draw_bridge");
    public static final DrawBridge SPRUCE_DRAW_BRIDGE = registerDrawBridge("spruce_draw_bridge");


    private static DoorBlock register(String key){
        MoreDoor newDoor = new MoreDoor(key);
        BlockItem item = new BlockItem(newDoor,new Item.Properties().tab(ModSetup.itemGroup));
        item.setRegistryName(key);
        System.out.println("-----------------Block "+key+" registered !------------------");
        System.out.println("------------------ Registry Name : "+newDoor.getRegistryName()+"-------------");
        DOORS.add(newDoor);
        DOOR_ITEMS.add(item);
        return newDoor;

    }

    private static WindowBlock registerWindows(String key){
        WindowBlock newWindow = new WindowBlock(key);
        BlockItem item = new BlockItem(newWindow,new Item.Properties().tab(ModSetup.itemGroup));
        item.setRegistryName(key);
        System.out.println("-----------------Block "+key+" registered !------------------");
        System.out.println("------------------ Registry Name : "+newWindow.getRegistryName()+"-------------");
        WINDOWS.add(newWindow);
        WINDOWS_ITEMS.add(item);
        return newWindow;
    }

    private static GarageDoor registerGarage(String key){
        GarageDoor garageDoor = new GarageDoor(key);
        BlockItem item = new GarageDoorItem(garageDoor);
        item.setRegistryName(key);
        System.out.println("-----------------Block "+key+" registered !------------------");
        System.out.println("------------------ Registry Name : "+garageDoor.getRegistryName()+"-------------");
        GARAGES.add(garageDoor);
        GARAGE_ITEMS.add(item);
        return garageDoor;
    }

    private static LargeDoor registerLargeDoor(String key, Material material){
        LargeDoor largeDoor = new LargeDoor(key, material);
        BlockItem item = new BlockItem(largeDoor,new Item.Properties().tab(ModSetup.itemGroup));
        item.setRegistryName(key);
        System.out.println("-----------------Block "+key+" registered !------------------");
        System.out.println("------------------ Registry Name : "+largeDoor.getRegistryName()+"-------------");
        LARGE_DOORS.add(largeDoor);
        LARGE_DOOR_ITEMS.add(item);
        return largeDoor;
    }

    private static GardenDoor registerGardenDoor(String key){
        GardenDoor gardenDoor = new GardenDoor(key);
        BlockItem item = new BlockItem(gardenDoor,new Item.Properties().tab(ModSetup.itemGroup));
        item.setRegistryName(key);
        System.out.println("-----------------Block "+key+" registered !------------------");
        System.out.println("------------------ Registry Name : "+gardenDoor.getRegistryName()+"-------------");
        GARDEN_DOORS.add(gardenDoor);
        GARDEN_DOOR_ITEMS.add(item);
        return gardenDoor;
    }

    private static DrawBridge registerDrawBridge(String key){
        DrawBridge drawBridge = new DrawBridge(key);
        BlockItem item = new BlockItem(drawBridge,new Item.Properties().tab(ModSetup.itemGroup));
        item.setRegistryName(key);
        System.out.println("-----------------Block "+key+" registered !------------------");
        System.out.println("------------------ Registry Name : "+drawBridge.getRegistryName()+"-------------");
        DRAW_BRIDGES.add(drawBridge);
        DRAW_BRIDGE_ITEMS.add(item);
        return drawBridge;
    }

    public static void registerBlocks(RegistryEvent.Register<Block> event){
        DOORS.forEach(event.getRegistry()::register);
        WINDOWS.forEach(windowBlock -> {
            event.getRegistry().register(windowBlock);
            //1.15 function

            if (FMLEnvironment.dist== Dist.CLIENT){
                ItemBlockRenderTypes.setRenderLayer(windowBlock, RenderType.cutoutMipped()); // for windows transparency
            }



        });
        GARAGES.forEach(doorBlock -> event.getRegistry().register(doorBlock));
        LARGE_DOORS.forEach(largeDoor -> event.getRegistry().register(largeDoor));
        GARDEN_DOORS.forEach(gardenDoor -> event.getRegistry().register(gardenDoor));
        DRAW_BRIDGES.forEach(drawBridge -> event.getRegistry().register(drawBridge));
        DOORS.clear();
        WINDOWS.clear();
        GARAGES.clear();
        LARGE_DOORS.clear();
        GARDEN_DOORS.clear();
        DRAW_BRIDGES.clear();
    }

    public static void registerItems(RegistryEvent.Register<Item> event){
        DOOR_ITEMS.forEach(item -> event.getRegistry().register(item));
        WINDOWS_ITEMS.forEach(item -> event.getRegistry().register(item));
        GARAGE_ITEMS.forEach(item -> event.getRegistry().register(item));
        LARGE_DOOR_ITEMS.forEach(item -> event.getRegistry().register(item));
        GARDEN_DOOR_ITEMS.forEach(item -> event.getRegistry().register(item));
        DRAW_BRIDGE_ITEMS.forEach(item -> event.getRegistry().register(item));
        DOOR_ITEMS.clear();
        WINDOWS_ITEMS.clear();
        GARAGE_ITEMS.clear();
        LARGE_DOOR_ITEMS.clear();
        GARDEN_DOOR_ITEMS.clear();
        DRAW_BRIDGE_ITEMS.clear();
    }

}
