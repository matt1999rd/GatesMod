
def create_item_json(list_item):
    i=0.0
    for item_name in list_item:
        with open(item_name+".json",'w') as json_file:
            content = '{\n    "parent": "item/generated",\n    "textures": {\n        "layer0": "gates:item/'+item_name+'"\n    }\n}'
            json_file.write(content)
            json_file.close()
        i+= 1.0
        print("Progress : "+ str(int(i*100/len(list_item)*100)/100.0)+"%" )
        

create_item_json(["oak_large_door","dark_oak_large_door","spruce_large_door","birch_large_door","jungle_large_door","acacia_large_door"])
    