

def create_doc(list_text):
    i=0.0
    for text in list_text:
        print("writing the four documents for the texture : "+text+"...")    
        with open(text+"_window_bottom.json","w") as json_file:
            content = get_content("bottom",text)
            json_file.write(content)
            json_file.close()
            print("file "+text+"_window_bottom.json created !")
        with open(text+"_window_bottom_open.json","w") as json_file:
            content = get_content("bottom_open",text)
            json_file.write(content)
            json_file.close()
            print("file "+text+"_window_bottom_open.json created !")
        with open(text+"_window_top.json","w") as json_file:
            content = get_content("top",text)
            json_file.write(content)
            json_file.close()
            print("file "+text+"_window_top.json created !")
        with open(text+"_window_top_open.json","w") as json_file:
            content = get_content("top_open",text)
            json_file.write(content)
            json_file.close()
            print("file "+text+"_window_top_open.json created !")
    
            
        """
        with open(color+"_door_bottom.json","w") as json_file:
            content = '{\n"parent": "block/door_bottom"'+com_content
            json_file.write(content)
            json_file.close()
            print("file "+color+"_door_bottom.json created !")
        
        with open(color+"_door_bottom_hinge.json","w") as json_file:
            content = '{\n"parent": "block/door_bottom_rh"'+com_content
            json_file.write(content)
            json_file.close()
            print("file "+color+"_door_bottom_hinge.json created !")
            
        with open(color+"_door_top.json","w") as json_file:
            content = '{\n"parent": "block/door_top"'+com_content
            json_file.write(content)
            json_file.close()
            print("file "+color+"_door_top.json created !")
         
        with open(color+"_door_top_hinge.json","w") as json_file:
            content = '{\n"parent": "block/door_top_rh"'+com_content
            json_file.write(content)
            json_file.close()
            print("file "+color+"_door_top_hinge.json created !")
        """ 
        i+=1.0
        print("advancement of the copy :  "+str(i/float(len(list_text))*100.0)+"%")

def get_content(string,text):
    return '{\n    "parent": "gates:block/window_'+string+'",\n    "textures": {\n        "bottom": "gates:block/'+text+'",\n        "top": "gates:block/'+text+'"\n    }\n}\n'

create_doc(["white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"])

