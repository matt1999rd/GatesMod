# -*- coding: utf-8 -*-
"""
Created on Fri Dec 18 19:55:20 2020

@author: PC
"""

def makeLargeDoorFile(wood_type):
    blockstate = ["ld","ld_open","rd","rd_open","lc","lc_open","rc","rc_open","lu","lu_open","ru","ru_open"]
    for bs in blockstate:
        with open(wood_type+"_large_door_"+bs+".json","w") as json_file:
            json_file.write('{\n"parent": ')
            json_file.write('"gates:block/large_door_'+bs+'",\n')
            json_file.write('"textures": {\n"wood": "gates:block/'+wood_type+'_large_door"')
            json_file.write('}\n}')
            json_file.close()
        print("blockstate json written !! ")

makeLargeDoorFile("acacia")
    
    