package fr.mattmouss.gates.doors;

import fr.mattmouss.gates.items.GarageDoorItem;
import fr.mattmouss.gates.setup.ModSetup;
import fr.mattmouss.gates.windows.WindowBlock;
import net.minecraft.block.Block;

import net.minecraft.block.DoorBlock;


import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)

public class DoorRegister {

    private static final List<DoorBlock> DOORS = new ArrayList<>();
    private static final List<Item> DOOR_ITEMS = new ArrayList<>();
    private static final List<WindowBlock> WINDOWS = new ArrayList<>();
    private static final List<Item> WINDOWS_ITEMS = new ArrayList<>();
    private static final List<GarageDoor> GARAGES = new ArrayList<>();
    private static final List<Item> GARAGE_ITEMS = new ArrayList<>();

    //enregistrement des portes classiques
    //à base de pierre
    public static final DoorBlock ANDESITE_DOOR = register("andesite_door");
    public static final DoorBlock COBBLESTONE_DOOR = register("cobblestone_door");
    public static final DoorBlock DIORITE_DOOR = register("diorite_door");
    public static final DoorBlock GRANITE_DOOR = register("granite_door");
    public static final DoorBlock STONE_DOOR = register("stone_door");

    //à base de couleur
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

    //enregistrement des fenêtres classiques
    public static final WindowBlock SB_WINDOWS = Wregister("stone_bricks_window"); //ok
    public static final WindowBlock CB_WINDOWS = Wregister("cobblestone_window"); //ok
    public static final WindowBlock STONE_WINDOWS = Wregister("stone_window"); //ok
    public static final WindowBlock ANDESITE_WINDOWS = Wregister("andesite_window"); //ok
    public static final WindowBlock GRANITE_WINDOWS = Wregister("granite_window");  //ok
    public static final WindowBlock DIORITE_WINDOWS = Wregister("diorite_window"); //ok
    public static final WindowBlock BRICK_WINDOWS = Wregister("brick_window"); //ok

    //à base de couleur
    //public static final DoorBlock WINDOW = Wregister("_window");
    public static final WindowBlock BLACK_WINDOW = Wregister("black_window");
    public static final WindowBlock BLUE_WINDOW = Wregister("blue_window");
    public static final WindowBlock BROWN_WINDOW = Wregister("brown_window");
    public static final WindowBlock CYAN_WINDOW = Wregister("cyan_window");
    public static final WindowBlock GRAY_WINDOW = Wregister("gray_window");
    public static final WindowBlock GREEN_WINDOW = Wregister("green_window");
    public static final WindowBlock L_BLUE_WINDOW = Wregister("light_blue_window");
    public static final WindowBlock L_GRAY_WINDOW = Wregister("light_gray_window");
    public static final WindowBlock LIME_WINDOW = Wregister("lime_window");
    public static final WindowBlock MAGENTA_WINDOW = Wregister("magenta_window");
    public static final WindowBlock ORANGE_WINDOW = Wregister("orange_window");
    public static final WindowBlock PINK_WINDOW = Wregister("pink_window");
    public static final WindowBlock PURPLE_WINDOW = Wregister("purple_window");
    public static final WindowBlock RED_WINDOW = Wregister("red_window");
    public static final WindowBlock WHITE_WINDOW = Wregister("white_window");
    public static final WindowBlock YELLOW_WINDOW = Wregister("yellow_window");


    //enregistrement des premiers garages
    //public static final GarageDoor _GARAGE = Gregister("_garage");
    public static final GarageDoor IRON_GARAGE = Gregister("iron_garage");
    public static final GarageDoor ANDESITE_GARAGE = Gregister("andesite_garage");
    public static final GarageDoor GRANITE_GARAGE = Gregister("granite_garage");
    public static final GarageDoor DIORITE_GARAGE = Gregister("diorite_garage");
    public static final GarageDoor STONE_GARAGE = Gregister("stone_garage");
    public static final GarageDoor STONEBRICKS_GARAGE = Gregister("stone_bricks_garage");

    //à base de couleur
    //public static final GarageDoor _GARAGE = Gregister("_garage");
    public static final GarageDoor BLACK_GARAGE = Gregister("black_garage");
    public static final GarageDoor BLUE_GARAGE = Gregister("blue_garage");
    public static final GarageDoor BROWN_GARAGE = Gregister("brown_garage");
    public static final GarageDoor CYAN_GARAGE = Gregister("cyan_garage");
    public static final GarageDoor GRAY_GARAGE = Gregister("gray_garage");
    public static final GarageDoor GREEN_GARAGE = Gregister("green_garage");
    public static final GarageDoor L_BLUE_GARAGE = Gregister("light_blue_garage");
    public static final GarageDoor L_GRAY_GARAGE = Gregister("light_gray_garage");
    public static final GarageDoor LIME_GARAGE = Gregister("lime_garage");
    public static final GarageDoor MAGENTA_GARAGE = Gregister("magenta_garage");
    public static final GarageDoor ORANGE_GARAGE = Gregister("orange_garage");
    public static final GarageDoor PINK_GARAGE = Gregister("pink_garage");
    public static final GarageDoor PURPLE_GARAGE = Gregister("purple_garage");
    public static final GarageDoor RED_GARAGE = Gregister("red_garage");
    public static final GarageDoor WHITE_GARAGE = Gregister("white_garage");
    public static final GarageDoor YELLOW_GARAGE = Gregister("yellow_garage");




    private static DoorBlock register(String key){
        MoreDoor newDoor = new MoreDoor(key);
        BlockItem item = new BlockItem(newDoor,new Item.Properties().group(ModSetup.itemGroup));
        item.setRegistryName(key);
        System.out.println("-----------------Block "+key+" registered !------------------");
        System.out.println("------------------ Registry Name : "+newDoor.getRegistryName()+"-------------");
        DOORS.add(newDoor);
        DOOR_ITEMS.add(item);
        return newDoor;

    }

    private static WindowBlock Wregister(String key){
        WindowBlock newWindow = new WindowBlock(key);
        BlockItem item = new BlockItem(newWindow,new Item.Properties().group(ModSetup.itemGroup));
        item.setRegistryName(key);
        System.out.println("-----------------Block "+key+" registered !------------------");
        System.out.println("------------------ Registry Name : "+newWindow.getRegistryName()+"-------------");
        WINDOWS.add(newWindow);
        WINDOWS_ITEMS.add(item);
        return newWindow;
    }

    private static GarageDoor Gregister(String key){
        GarageDoor garageDoor = new GarageDoor(key);
        BlockItem item = new GarageDoorItem(garageDoor);
        item.setRegistryName(key);
        System.out.println("-----------------Block "+key+" registered !------------------");
        System.out.println("------------------ Registry Name : "+garageDoor.getRegistryName()+"-------------");
        GARAGES.add(garageDoor);
        GARAGE_ITEMS.add(item);
        return garageDoor;
    }


    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event){
        DOORS.forEach(doorBlock -> {
            event.getRegistry().register(doorBlock);
        });
        WINDOWS.forEach(windowBlock -> {
            event.getRegistry().register(windowBlock);
            //1.15 function
            /*
            if (FMLEnvironment.dist == Dist.CLIENT){
                RenderTypeLookup.setRenderLayer(windowBlock, RenderType.getCutout()); // pour la transparence des fenetres
            }
             */
        });
        GARAGES.forEach(doorBlock -> {
            event.getRegistry().register(doorBlock);
        });
        DOORS.clear();
        WINDOWS.clear();
        GARAGES.clear();
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event){
        DOOR_ITEMS.forEach(item -> {
            event.getRegistry().register(item);
        });
        WINDOWS_ITEMS.forEach(item -> {
            event.getRegistry().register(item);
        });
        GARAGE_ITEMS.forEach(item -> {
            event.getRegistry().register(item);
        });
        DOOR_ITEMS.clear();
        WINDOWS_ITEMS.clear();
        GARAGE_ITEMS.clear();

    }

}
