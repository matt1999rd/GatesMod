
def create_item_json(list_item):
    i=0.0
    for item_name in list_item:
        with open(item_name+".json",'w') as json_file:
            content = '{\n    "parent": "item/generated",\n    "textures": {\n        "layer0": "gates:item/'+item_name+'"\n    }\n}'
            json_file.write(content)
            json_file.close()
        i+= 1.0
        print("Progress : "+ str(int(i*100/len(list_item)*100)/100.0)+"%" )
        

create_item_json(["white_garage","orange_garage","magenta_garage","light_blue_garage","yellow_garage","lime_garage","pink_garage","gray_garage","light_gray_garage","cyan_garage","purple_garage","blue_garage","brown_garage","green_garage","red_garage","black_garage"])
    