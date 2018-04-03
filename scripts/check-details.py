#!/usr/bin/env python

import os
import glob
import json

if __name__ == "__main__":

    total = 0
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

        consolidated = []

        for pkg in pkgs:
            pkg = pkg.strip()
            if pkg in mapview:
                consolidated.append(mapview[pkg])

        with open(fn, "w") as f:
            json.dump(consolidated, f, indent = 2)

        total = total + len(consolidated)

        print "%s %d" % (anum, len(consolidated))

    print "%d collected" % total
