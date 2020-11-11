
def createLootTable(block_id):
    with open(block_id+".json","w") as json_file:
        json_file.write('{\n  "type": "minecraft:block",\n  "pools": [\n    {\n      "name": "pool",')
        json_file.write('\n      "rolls": 1,\n      "entries": [\n        {\n          "type": "minecraft:item",')
        json_file.write('\n          "name": "gates:'+block_id)
        json_file.write('"\n        }\n      ],\n      "conditions": [\n        {\n          ')
        json_file.write('"condition": "minecraft:survives_explosion"\n        }\n      ]\n    }\n  ]\n}')
    print("file json loot table : "+block_id+".json written !!")

material = ["andesite","cobblestone","diorite","granite","stone_bricks","stone"]

block = ["window","garage"]

colour = ["black","blue","brown","cyan","gray","green","light_blue","light_gray","lime","magenta","orange","pink","purple","red","white","yellow"]
n=(len(material)+len(colour))*2
i=0.0
for b in block:
    for m in material:
        createLootTable(m+"_"+b)
        i+=1.0
        print ("progress :"+str(i/n*100)+" %")
    for c in colour:
        createLootTable(c+"_"+b)
        i+=1.0
        print ("progress :"+str(i/n*100)+" %")
    
