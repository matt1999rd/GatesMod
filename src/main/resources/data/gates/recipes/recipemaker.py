
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
        m=len(dic)
        i=0
        for key in dic.keys():
            json_file.write('"'+key+'": {\n      "item": "minecraft:'+dic.get(key)+'"\n    }')
            i+=1
            if i==m-1:
                json_file.write(",\n")
        #end of file and defining count
        json_file.write('\n },"result": {\n    "item": "gates:'+material+"_"+blockName+'",\n    "count": '+str(nb_count)+'\n  }\n}')
    print("recipes written !!")
        
material = ["andesite","cobblestone","diorite","granite","stone_bricks","stone"]

colour = ["black","blue","brown","cyan","gray","green","light_blue","light_gray","lime","magenta","orange","pink","purple","red","white","yellow"]

blockParameter = ["garden_door",16,"igi","i#i","igi",{"i":"iron_ingot","g":"iron_bars"}]

n=len(colour)
i=0.0

for c in colour:
    makeRecipe(c,blockParameter,True)
    i+=1.0
    print ("progress :"+str(i/n*100)+" %")

