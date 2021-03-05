
def createLootTable(block_id):
    with open(block_id+".json","w") as json_file:
        json_file.write('{\n  "type": "minecraft:block",\n  "pools": [\n    {\n      "name": "pool",')
        json_file.write('\n      "rolls": 1,\n      "entries": [\n        {\n          "type": "minecraft:item",')
        json_file.write('\n          "name": "gates:'+block_id)
        json_file.write('"\n        }\n      ],\n      "conditions": [\n        {\n          ')
        json_file.write('"condition": "minecraft:survives_explosion"\n        }\n      ]\n    }\n  ]\n}')
    print("file json loot table : "+block_id+".json written !!")


def createLootTableWithSpawnCondition(block_id,property_condition):
    with open(block_id+".json","w") as json_file:
        json_file.write('{\n  "type": "minecraft:block",\n  "pools": [\n    {\n      "name": "pool",')
        json_file.write('\n      "rolls": 1,\n      "entries": [\n        {\n          "type": "minecraft:item",')
        json_file.write('"conditions": [\n            {\n              "condition": "minecraft:block_state_property",')
        json_file.write('\n              "block": "gates:'+block_id+'",\n')
        json_file.write('      "properties": {\n                '+property_condition+'\n}\n}\n],')
        json_file.write('\n          "name": "gates:'+block_id)
        json_file.write('"\n        }\n      ],\n      "conditions": [\n        {\n          ')
        json_file.write('"condition": "minecraft:survives_explosion"\n        }\n      ]\n    }\n  ]\n}')
    print("file json loot table : "+block_id+".json written !!")

#material = ["andesite","cobblestone","diorite","granite","stone_bricks","stone"]

#block = ["door","garage"]

#property_condition = '"placing": "up_left"'

colour = ["black","blue","brown","cyan","gray","green","light_blue","light_gray","lime","magenta","orange","pink","purple","red","white","yellow"]
i=0.0
n=len(colour)
for c in colour:
    createLootTable(c+"_garden_door")
    i+=1.0
    print ("progress :"+str(i/n*100)+" %")
createLootTable("iron_large_door")
createLootTable("haussmann_large_door")
createLootTable("haussmann2_large_door")

'''
n=(len(material)+len(colour))
i=0.0
for m in material:
    createLootTableWithSpawnCondition(m+"_garage",property_condition)
    i+=1.0
    print ("progress :"+str(i/n*100)+" %")
for c in colour:
    createLootTableWithSpawnCondition(c+"_garage",property_condition)
    i+=1.0
    print ("progress :"+str(i/n*100)+" %")
'''
