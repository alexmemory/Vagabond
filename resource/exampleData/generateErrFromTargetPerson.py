#!/usr/bin/env python
# encoding: utf-8
"""
generateData.py

Created by Jiang on 2012-02-20.
Copyright (c) 2012 __Jiang Du__. All rights reserved.

This program generates data for the schema defined in homeless.xml.
"""

import sys
import getopt
import random, string
import psycopg2
import math


help_message = '''
The help message goes here.
'''

maxInt = 100000
maxCharLength = 50

class Usage(Exception):
    def __init__(self, msg):
        self.msg = msg

    
def generateTables():
    tableData = generateErrMarkers()
    writeToFile(tableData, 'errMarkers.csv')
        

def writeToFile(lineList, fileName):
    outFile = file(fileName, 'w')
    lineList = map(lambda(x): str(x)+'\n', lineList)
    outFile.writelines(lineList)
    outFile.close()
    

def getTidList():
    tidList = []
    conn = psycopg2.connect("dbname=tramptest user=jiang")
    cur = conn.cursor()
    cur.execute("select tid from target.person")
    tidList = cur.fetchall()
    cur.close()
    conn.close()
    return map(lambda(x): str(x)[2:-3], tidList)
    
    
def generateErrMarkers(errPercentage=10):
    data = []
    tidList = getTidList()
    errTidList = random.sample(tidList, len(tidList)*errPercentage/100)
    maxBitValue = 3 # 4 bits: 1111
    maxDigits = int(math.sqrt(maxBitValue+1))
    for errTid in errTidList:
        attrValue = random.randint(1, maxBitValue)
        errMarker = bin(attrValue)[2:]
        if len(errMarker) < maxDigits:
            errMarker = '0'*(maxDigits-len(errMarker))+errMarker
        data.append('person,'+errTid+','+errMarker)
    return data
    
    
def main(argv=None):
    if argv is None:
        argv = sys.argv
    try:
        try:
            opts, args = getopt.getopt(argv[1:], "ho:v", ["help", "output="])
        except getopt.error, msg:
            raise Usage(msg)
    
        # option processing
        for option, value in opts:
            if option == "-v":
                verbose = True
            if option in ("-h", "--help"):
                raise Usage(help_message)
            if option in ("-o", "--output"):
                output = value
                
        generateTables()
    
    except Usage, err:
        print >> sys.stderr, sys.argv[0].split("/")[-1] + ": " + str(err.msg)
        print >> sys.stderr, "\t for help use --help"
        return 2




if __name__ == "__main__":
    sys.exit(main())
