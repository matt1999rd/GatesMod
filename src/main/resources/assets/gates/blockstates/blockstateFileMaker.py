#list_bs = ["facing","open","window_place","rotated"]
#list_pv = [["east","north","south","west"],[True,False],["full","both_up","both_down","both_right","both_left","both_middle","down_left","down_right","up_left","up_right","middle_left","middle_right"],[True,False]]
#material = ["andesite","black","blue","brick","brown","cobblestone","cyan","diorite","granite","gray","green","light_blue","light_gray","lime","magenta","orange","pink","purple","red","stone_bricks","stone","white","yellow"]
color = ["black","blue","brown","cyan","gray","green","light_blue","light_gray","lime","magenta","orange","pink","purple","red","white","yellow"]

#file_name="andesite_window"

def createBlockStateJson(file_name,**states):
    begining = '{\n"variants": {\n'
    end = '  }\n}'
    center_string = create_center_string(file_name,**states)
    with open(file_name+".json","w") as json_file:
        json_file.write(begining)
        json_file.write(center_string)
        json_file.write(end)
        json_file.close()
    print("blockstate json written !! ")
                
def create_center_string(file_name,**states):
    n=0
    for value in states.values():
        n*=len(value)
    s=execLoop(file_name,{},states)
    return s[:-2]

def execLoop(file_name,defined_states,non_defined_states):
    print("\n execloop with defined states :")
    print(defined_states)
    print("non defined states :")
    print(non_defined_states)
    keys=list(non_defined_states.keys())
    property=[]
    this_keys=""
    if keys:
        property=non_defined_states.get(keys[0])
        this_keys = keys[0]
    else:
        return getLineModel(defined_states)
    remaining_states = {}
    for k in keys:
        if k != keys[0]:
            remaining_states[k]=non_defined_states.get(k)
    s=""
    for p in property:
        new_defined_states = defined_states.copy()
        if this_keys == "": return
        new_defined_states[this_keys] = p
        s+=execLoop(file_name,new_defined_states,remaining_states)
    print("string obtained : "+s)
    return s

"""
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
"""

def getModel(file_name,position,open_bool):
    model_str= "gates:block/"+file_name+"_"+getInitial(position)
    if open_bool == "true":
        model_str+="_open"
    return model_str 

def getLineModel(defined_states):
    keys=defined_states.keys()
    n=len(keys)
    line="   \""
    i=0
    for k in keys:
        if i!=0:
            line +=","
        line += k+"="+defined_states.get(k)
        f= defined_states.get("facing")
        facing_add_on = ""
        if f!="south":
            facing_add_on += ', "y":'
        if f=="west":
            facing_add_on +='90'
        elif f=="north":
            facing_add_on +='180'
        elif f=="east":
            facing_add_on +='270'
        if i==n-1:
            line+="\":  { \"model\": "+DBModel(defined_states.get("position"),int(defined_states.get("animation")))+facing_add_on+"},"
        i+=1
    line+="\n"
    return line

def DBModel(pos,animState):
    if needAir(pos,animState):
        return "\"block/air\""
    if needSupport(pos,animState):
        if "left" in pos:
            return "\"gates:block/draw_bridge_left_support\""
        return "\"gates:block/draw_bridge_right_support\""
    if pos in ["door_left_up_up","door_right_up_up"]:
        animationEnd = ""
    else:
        animationEnd = getAnimationEnd(int(animState))
    return "\"gates:block/draw_bridge_"+getInitial(pos) + animationEnd +"\""

def needAir(pos,animState):
    if pos in ["bridge_left","bridge_right"]:
        return True
    if pos in ["bridge_ext_left","bridge_ext_right"]:
        return animState<3
    return False

def needSupport(pos,animState):
    if pos in ["door_left_down","door_right_down"]:
        return True
    if pos in ["door_left_up","door_right_up"]:
        return animState>2
    return False

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

def getAnimationEnd(anim_state):
    if anim_state>4:
        return ""
    angle=anim_state*225
    return "_"+str(angle)

createBlockStateJson("draw_bridge",
                     facing=["east","north","south","west"],
                     position=["door_left_down","door_left_up",
                               "door_right_down","door_right_up",
                               "door_left_up_up","door_right_up_up",
                               "bridge_left","bridge_right",
                               "bridge_ext_left","bridge_ext_right"],
                     animation=["0","1","2","3","4"],
                     powered=["false","true"])

'''
n=float(len(color))
incr=0.0
for c in color:
	createBlockStateJson(["east","north","south","west"],["left_down","left_up","right_down","right_up"],["true","false"],c+"_garden_door")
	print("Creating json blockstate file : "+c+"_garden_door.json !")
	print("Progress : "+str(incr/n*100)+" %")
	incr+=1
'''

'''
n= float(len(material))
incr=0.0
for m in material:
    createBlockStateJson(list_pv[0],list_pv[1],list_pv[2],list_pv[3],m+"_window")
    print("Creating json blockstate file : "+m+"_window.json !")
    print("Progress : "+str(incr/n*100)+" %")
    incr+=1
'''
            
            
        