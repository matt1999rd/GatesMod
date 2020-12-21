#list_bs = ["facing","open","window_place","rotated"]
#list_pv = [["east","north","south","west"],[True,False],["full","both_up","both_down","both_right","both_left","both_middle","down_left","down_right","up_left","up_right","middle_left","middle_right"],[True,False]]
#material = ["andesite","black","blue","brick","brown","cobblestone","cyan","diorite","granite","gray","green","light_blue","light_gray","lime","magenta","orange","pink","purple","red","stone_bricks","stone","white","yellow"]

#file_name="andesite_window"

def createBlockStateJson(facing,animation,position,file_name):
    begining = '{\n"variants": {\n'
    end = '  }\n}'
    center_string = create_center_string(facing,animation,position,file_name)
    with open(file_name+".json","w") as json_file:
        json_file.write(begining)
        json_file.write(center_string)
        json_file.write(end)
        json_file.close()
    print("blockstate json written !! ")
                

def create_center_string(facing,position,open_tab,file_name):
    center_string=""
    incr = 0
    n = len(facing)*len(position)*len(open_tab)
    for f in facing:
        for pos in position:
            for open_bool in open_tab:
                line='"facing='+f+",position="+pos+",open="+open_bool+'":  { "model": "'+getModel(file_name,pos,open_bool)+'"'
                if f!="east":
                    line += ', "y":'
                if f=="west":
                    line +='180'
                elif f=="north":
                    line +='270'
                elif f=="south":
                    line +='90'
                line+=' }'
                incr += 1
                if (incr != n):
                    line +=','
                center_string += line + "\n"
    return center_string
    
def getModel(file_name,position,open_bool):
    model_str= "gates:block/"+file_name+"_"+getInitial(position)
    if (open_bool == "true"):
        model_str+="_open"
    return model_str 
   

def getInitial(st):
    if (len(st) == 0):
        return ""
    initial = ""
    i=0
    is_previous_char_underscore = True
    while(i<len(st)):
        if (is_previous_char_underscore):
            initial += st[i]
        if (st[i] == "_"):
            is_previous_char_underscore = True
        else:
            is_previous_char_underscore = False
        i+=1
    return initial

createBlockStateJson(["east","north","south","west"],["left_down","left_center","left_up","right_down","right_center","right_up"],["true","false"],"oak_large_door")
'''
n= float(len(material))
incr=0.0
for m in material:
    createBlockStateJson(list_pv[0],list_pv[1],list_pv[2],list_pv[3],m+"_window")
    print("Creating json blockstate file : "+m+"_window.json !")
    print("Progress : "+str(incr/n*100)+" %")
    incr+=1
'''
            
            
        