#!/usr/bin/env python

import os
import glob
import json
from datetime import datetime
from collections import OrderedDict

if __name__ == "__main__":

    combine = []

    files = glob.glob("../accounts/*/apps.json")
    for fn in files:
        adir = os.path.dirname(fn) 
        anum = os.path.basename(adir) 

        with open(fn, "r") as f:
            data = json.load(f)

        mapview = dict()
        for item in data:
            mapview[item["pkgName"]] = item

        with open(os.path.join(adir, "sorted.list"), "r") as f:
            pkgs = f.readlines()

        for pkg in pkgs:
            pkg = pkg.strip()
            if pkg in mapview:
                combine.append(mapview[pkg])

    print "%d collected" % len(combine) 

    hist = dict()
    for info in combine:
        date = datetime.strptime(info["update"], "%b %d, %Y")
        if date not in hist:
            hist[date] = []
        
        hist[date].append(info)

    ordered = OrderedDict(sorted(hist.items(), reverse = True))

    for k in ordered:
        print str(k) + " " + str(len(ordered[k]))
