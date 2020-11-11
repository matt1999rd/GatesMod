list_bs = ["facing","open","window_place"]
list_pv = [["east","north","south","west"],[True,False],["full","both_up","both_down","both_right","both_left","both_middle","down_left","down_right","up_left","up_right","middle_left","middle_right"]]
material = ["andesite","black","blue","brick","brown","cobblestone","cyan","diorite","granite","gray","green","light_blue","light_gray","lime","magenta","orange","pink","purple","red","stone_bricks","stone","white","yellow"]

#file_name="andesite_window"

def createBlockStateJson(facing,open_val,window_place,file_name):
    begining = '{\n"variants": {\n'
    end = '  }\n}'
    center_string = create_center_string(facing,open_val,window_place,file_name)
    with open(file_name+".json","w") as json_file:
        json_file.write(begining)
        json_file.write(center_string)
        json_file.write(end)
        json_file.close()
    print("blockstate json written !! ")
                

def create_center_string(facing,open_val,window_place,file_name):
    center_string=""
    incr = 0
    n = len(facing)*len(open_val)*len(window_place)
    for f in facing:
        for ov in open_val:
            for wp in window_place:
                ov_str=''
                if ov:
                    ov_str = 'true'
                else:
                    ov_str = 'false'
                line='"facing='+f+",open="+ov_str+",window_place="+wp+'":  { "model": "'+getModel(file_name,ov,wp)+'"'
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
    
def getModel(file_name,open_val,window_place):
    if (open_val):
        return "gates:block/"+file_name+"_"+getInitial(window_place)+"_open"
    return "gates:block/"+file_name+"_"+getInitial(window_place)
   

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

n= float(len(material))
incr=0.0
for m in material:
    createBlockStateJson(list_pv[0],list_pv[1],list_pv[2],m+"_window")
    print("Creating json blockstate file : "+m+"_window.json !")
    print("Progress : "+str(incr/n*100)+" %")
    incr+=1
            
            
        