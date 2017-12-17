id=0
MACHINE_DICT = {"Machines":[["Maschine1","/machine/","1","Berlin","50"," "],
                            ["Maschine2","/machine/","2","Hamburg","50"," "],
                            ["Maschine3","/machine/","3","Berlin","50"," "]]
                  }

def Content():
	return MACHINE_DICT

def addContent(machine_name, facility, sonstiges, id):
	MACHINE_DICT2 = {"Machines":[[machine_name,"/machine/",id,facility,"50",sonstiges]]
				}
	return MACHINE_DICT2