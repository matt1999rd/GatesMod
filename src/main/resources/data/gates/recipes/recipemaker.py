
def makeRecipe(material,blockParameter,isColour):
    blockName = blockParameter[0]
    nb_count = blockParameter[1]
    first_row = blockParameter[2]
    second_row = blockParameter[3]
    third_row = blockParameter[4]
    dic = blockParameter[5]
    with open(material+"_"+blockName+".json","w") as json_file:
        json_file.write('{\n  "type": "minecraft:crafting_shaped",\n  "pattern"')
        json_file.write(': [\n    "'+first_row+'",\n    "'+second_row+'",\n    "'+third_row+'"\n  ],\n  "key": {\n')
        #keys defining
        if isColour:
            json_file.write('"#": {\n      "item": "minecraft:'+material+'_dye"\n    },\n')
        else:
            json_file.write('"#": {\n      "item": "minecraft:'+material+'"\n    },\n')
        for key in dic.keys():
            json_file.write('"'+key+'": {\n      "item": "minecraft:'+dic.get(key)+'"\n    }\n },')
        #end of file and defining count
        json_file.write('"result": {\n    "item": "gates:'+material+"_"+blockName+'",\n    "count": '+str(nb_count)+'\n  }\n}')
    print("recipes written !!")
        
material = ["andesite","cobblestone","diorite","granite","stone_bricks","stone"]

colour = ["black","blue","brown","cyan","gray","green","light_blue","light_gray","lime","magenta","orange","pink","purple","red","white","yellow"]

blockParameter = ["garage",16,"iii","i#i","iii",{"i":"iron_ingot"}]

n=len(material)+len(colour)
i=0.0
for m in material:
    makeRecipe(m,blockParameter,False)
    i+=1.0
    print ("progress :"+str(i/n*100)+" %")

for c in colour:
    makeRecipe(c,blockParameter,True)
    i+=1.0
    print ("progress :"+str(i/n*100)+" %")

