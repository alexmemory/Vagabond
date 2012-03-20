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
import os


help_message = '''
The help message goes here.
'''

maxInt = 100000
maxCharLength = 50

class Usage(Exception):
    def __init__(self, msg):
        self.msg = msg


def connect2DB():
    conn = psycopg2.connect("dbname=tramptest user=jiang")
    return conn


def closeDB(conn):
    conn.close()
    
    
def load2DB(conn, fullPath):
    try:
        cur = conn.cursor()
        cur.execute("drop table if exists errm")
        cur.execute("create table errm (rel text, tid text, att bit varying(2))")
        cur.execute("copy errm from '"+fullPath+"' delimiter ','")
        cur.execute("VACUUM FULL ANALYZE")
        conn.commit()
    except:
        print('errors in loading to the table')
    else:
        cur.close()

        
def generateTables(conn, percentage):
    tableData = generateErrMarkers(connection = conn, errPercentage = percentage)
    fullPath = os.path.realpath('./errMarkers.csv');
    writeToFile(tableData, '../../build/macloader/resource/exampleData/errMarkers.csv')
    writeToFile(tableData, fullPath)
    return fullPath
        

def writeToFile(lineList, fileName):
    outFile = file(fileName, 'w')
    lineList = map(lambda(x): str(x)+'\n', lineList)
    outFile.writelines(lineList)
    outFile.close()
    

def getTidList(conn):
    tidList = []
    try:
        cur = conn.cursor()
        cur.execute("select tid from target.person")
        tidList = cur.fetchall()
    except err:
        pass
    else:
        cur.close()
    return map(lambda(x): str(x)[2:-3], tidList)
    
    
def generateErrMarkers(connection, errPercentage):
    data = []
    tidList = getTidList(connection)
    errTidList = random.sample(tidList, len(tidList)*errPercentage/100)
    maxBitValue = 3 # 2 bits: 11
    maxDigits = int(math.sqrt(maxBitValue+1))
    for errTid in errTidList:
        attrValue = random.randint(1, maxBitValue)
        errMarker = bin(attrValue)[2:]
        if len(errMarker) < maxDigits:
            errMarker = '0'*(maxDigits-len(errMarker))+errMarker # padding
        data.append('person,'+errTid+','+errMarker)
    return data
    
    
def main(argv=None):
    percentage = 10 # default value
    if argv is None:
        argv = sys.argv
    try:
        try:
            opts, args = getopt.getopt(argv[1:], "hop:v", ["help", "output=", "percentage="])
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
            if option in ("-p", "--percentage"):
                percentage = value
        
        try:
            conn = connect2DB()
            fullPath = generateTables(conn, percentage)
            load2DB(conn, fullPath)
        except err:
            pass
        else:
            closeDB(conn)
    
    except Usage, err:
        print >> sys.stderr, sys.argv[0].split("/")[-1] + ": " + str(err.msg)
        print >> sys.stderr, "\t for help use --help"
        return 2




if __name__ == "__main__":
    sys.exit(main())
