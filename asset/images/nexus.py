#!/usr/bin/env python

import os
import sys
import urllib
from bs4 import BeautifulSoup as Soup

BRAND = "nexus"

if __name__ == "__main__":

    if len(sys.argv) < 3:
        sys.exit("Usage: %s <html-file> <download-path>" % sys.argv[0])

    fn = sys.argv[1]
    dn = sys.argv[2]

    with open(fn, "r") as f:
        soup = Soup(f, "html.parser")

    divs = soup.findAll("div", {"class": "devsite-table-wrapper"})
    for div in divs:
        trs = div.find("tbody").findAll("tr")
        for tr in trs:
            td = tr.findAll("td")[0]
            vern = td.text.split(" ")[0]

            a = tr.find("a")
            link = a["href"]
            tokens = link.split("/")[-1].split("-")
            model = tokens[0]
            build = tokens[1]

            name = "%s-%s-%s-%s" % (BRAND, model, build, vern)

            print "Downloading %s" % name
            urllib.urlretrieve(link, os.path.join(dn, name + ".zip"))
