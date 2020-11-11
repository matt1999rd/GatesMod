# -*- coding: utf-8 -*-
"""
Created on Sat Feb 15 14:02:26 2020

@author: PC
"""
list_colour = ["white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"]

list_solid = ["andesite","granite","diorite","stone","stone_bricks"]+list_colour

def create_doc():
    i=0.0
    for color in list_colour:
        print("writing the four documents for the colour : "+color+"...")    
        #com_content = ',\n"textures": {\n"bottom": "gates:block/'+color+'_door_bottom",\n"top": "gates:block/'+color+'_door_top"\n}\n}'
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
        print("advancement of the copy :  "+str(i/float(len(list_colour))*100.0)+"%")


#create_doc()

def create_doc2():
    i=0.0
    for solid in list_solid:
        print("writing the six documents for the colour : "+solid+"...")
        com_content = ',\n"textures": {\n"bottom": "gates:block/'+solid+'_garage_down",\n"top": "gates:block/'+solid+'_garage_up"\n}\n}'
        with open(solid+"_garage_down_0.json","w") as json_file:
            content = '{\n"parent": "gates:block/garage_down_0"'+com_content
            json_file.write(content)
            json_file.close()
            print("file "+solid+"_garage_down_0.json created !")
        with open(solid+"_garage_down_45.json","w") as json_file:
            content = '{\n"parent": "gates:block/garage_down_45"'+com_content
            json_file.write(content)
            json_file.close()
            print("file "+solid+"_garage_down_45.json created !")
        with open(solid+"_garage_down_675.json","w") as json_file:
            content = '{\n"parent": "gates:block/garage_down_675"'+com_content
            json_file.write(content)
            json_file.close()
            print("file "+solid+"_garage_down_675.json created !")
        with open(solid+"_garage_up_0.json","w") as json_file:
            content = '{\n"parent": "gates:block/garage_up_0"'+com_content
            json_file.write(content)
            json_file.close()
            print("file "+solid+"_garage_up_0.json created !")
        with open(solid+"_garage_up_225.json","w") as json_file:
            content = '{\n"parent": "gates:block/garage_up_225"'+com_content
            json_file.write(content)
            json_file.close()
            print("file "+solid+"_garage_up_225.json created !")
        with open(solid+"_garage_up_45.json","w") as json_file:
            content = '{\n"parent": "gates:block/garage_up_45"'+com_content
            json_file.write(content)
            json_file.close()
            print("file "+solid+"_garage_up_45.json created !")
        i+=1.0
        print("advancement of the copy :  "+str(i/float(len(list_solid))*100.0)+"%")

create_doc2()