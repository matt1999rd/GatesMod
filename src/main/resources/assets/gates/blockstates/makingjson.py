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
                

def create_center_string(facing,animation,position,file_name):
    center_string=""
    incr = 0
    n = len(facing)*len(animation)*len(position)
    for f in facing:
        for anim in animation:
            for pos in position:
                line='"facing='+f+",animation="+anim+",position="+pos+'":  { "model": "'+getModel(file_name,pos,anim)+'"'
                if f!="north":
                    line += ', "y":'
                if f=="west":
                    line +='270'
                elif f=="east":
                    line +='90'
                elif f=="south":
                    line +='180'
                line+=' }'
                incr += 1
                if (incr != n):
                    line +=','
                center_string += line + "\n"
    return center_string
    
def getModel(file_name,position,animation):
    if (position == "center_down" or position == "center_up"):
        model_str = "block/air"
    else:
        model_str= "gates:block/"+file_name+"_"+getInitial(position)
        model_str+="_"+animation
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

createBlockStateJson(["east","north","south","west"],["0","1","2","3","4"],["left_down","left_up","center_down","center_up","right_down","right_up"],"window_door")
'''
n= float(len(material))
incr=0.0
for m in material:
    createBlockStateJson(list_pv[0],list_pv[1],list_pv[2],list_pv[3],m+"_window")
    print("Creating json blockstate file : "+m+"_window.json !")
    print("Progress : "+str(incr/n*100)+" %")
    incr+=1
'''
            
            
        