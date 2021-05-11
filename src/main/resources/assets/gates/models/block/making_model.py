# -*- coding: utf-8 -*-
"""
Created on Sun May 03 18:13:20 2020

@author: PC
"""

material = ["andesite","black","blue","brick","brown","cobblestone","cyan","diorite","granite","gray","green","light_blue","light_gray","lime","magenta","orange","pink","purple","red","stone_bricks","stone","white","yellow"]

window_place = ["f","f_open","bu","bu_open","bd","bd_open","br","br_open","bl","bl_open","bm","bm_open","dl","dl_open","dr","dr_open","ul","ul_open","ur","ur_open","ml","ml_open","mr","mr_open"]


def Json_write(m,wp):
    begining = '{\n    "parent": "gates:block/window_'
    texture_str = '    "textures": {\n      "material": "gates:block/'
    end = '\n    }\n}'
    with open(m+"_window_"+wp+".json","w") as json_file:
        json_file.write(begining)
        json_file.write(wp+'",\n')
        json_file.write(texture_str+m+'"')
        json_file.write(end)
    print("model written : "+m+"_window_"+wp+".json")
	
def buildJson(texture_key,texture_name,file_name,state):
	parent_file_name = file_name+"_"+state
	texture_loc= texture_name+"_"+file_name
	begining = "{\n    \"parent\": \"gates:block/"+parent_file_name+"\", \n"
	texture_str = '    "textures": {\n      "'+texture_key+'": "gates:block/'+texture_loc+"\""
	end = '\n    }\n}'
	with open(texture_name+"_"+parent_file_name+".json","w") as json_file:
		json_file.write(begining)
		json_file.write(texture_str)
		json_file.write(end)
	print("model written : "+parent_file_name+".json")
	
wood = ["oak","dark_oak","acacia","spruce","jungle","birch"]
draw_bridge_states=["dluu","druu","dlu_0","dlu_225","dlu_450","dru_0","dru_225","dru_450","left_support","right_support","bl_675","bl_900","br_675","br_900"]

n=len(draw_bridge_states)*len(wood)
i=0
for state in draw_bridge_states:
	for w in wood:
		buildJson("wood",w,"draw_bridge",state)
		i+=1
		print("Progress : "+str(int(i/n*100))+" %")

"""
n= float(len(material) * len(window_place))
incr = 0.0
for m in material:
    for wp in window_place:
        Json_write(m,wp)
        Json_write(m,wp+"_rot")
        incr+= 1
        print("Progress : "+str(int(incr/n*100))+" %")
		
"""
