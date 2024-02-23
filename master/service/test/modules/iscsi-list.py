#!/usr/bin/python3
if __name__ == "__main__":

    import sys
    import os

    targetcli = "/bin/targetcli"
    PATH_LVM_DISKS = "/dev/agapita-cluster/"
    PATH_ISCSI_DISKS = "/backstores/block/"
    PATH_IQN = "/iscsi/"
    PATH_IQN_LUNS = PATH_IQN+"/tpg1/luns"
    TEXT_HELP = "help: $0 <Name of conection> <Name of LVM disk>"

    if len(sys.argv) > 1:
        if sys.argv[1] == "-h" or sys.argv[1] == "-help" or sys.argv[1] == "--help":
            print(TEXT_HELP)
            sys.exit(0)

    data = ""
    salida = list()
    criba = ""

    #terminal = os.popen(targetcli+" "+PATH_IQN+" ls")
    terminal = os.popen("cat master/service/test/modules/example-targetcli-ls.txt")
    data = terminal.read()
    terminal.close()

    for i in data.split("\n"):
      if i.find("TPG") >= 0 or i.find("lun") >= 0:
        criba = criba + i
    data = criba
    data.replace("\n", " ")
    for i in data.split(" "):
      if i.find("iqn") >= 0:
        salida.append(list())
        salida[-1].append(i.split(".")[-1])
      if i.find("dev") >= 0:
        salida[-1].append(i.translate(i.maketrans("()","  ")).strip())

    print("NODE\tDISKS")
    for i in salida:
      tmp = ""
      for k in i:
        tmp = tmp + "\t" + k
      print(tmp.strip())