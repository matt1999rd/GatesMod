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

n= float(len(material) * len(window_place))
incr = 0.0
for m in material:
    for wp in window_place:
        Json_write(m,wp)
        incr+= 1
        print("Progress : "+str(int(incr/n*100))+" %")
        

