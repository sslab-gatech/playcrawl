#!/usr/bin/env python

import os
import sys
import glob
import time
import subprocess
from multiprocessing import Process

DELAY = 10

def launch(acc, cmd):
    command = "java -Dlog=%s -jar target/playcrawl.jar -c %s/account.json %s" % \
            (acc, acc, cmd)

    print command
    subprocess.call(command, shell = True) 


if __name__ == "__main__":

    accs = glob.glob("accounts/*")
    cmd = " ".join(sys.argv[1:])

    procs = []
    for acc in accs:
        pcmd = cmd.replace("ACCOUNT", acc)

        p = Process(target = launch, args = (acc, pcmd))
        p.start()
        procs.append(p)

        print "%s started" % acc 
        time.sleep(DELAY)

    for p in procs:
        p.join()
