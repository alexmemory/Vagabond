#!/usr/bin/env python
# encoding: utf-8
"""
generateData4BitVectors.py

Created by Jiang on 2012-03-05.
Copyright (c) 2012 __Jiang Du__. All rights reserved.

This program generates data for the schema defined in TestBitVectors_SL50_D100k.xml.
"""

import sys
import getopt
import random, string


help_message = '''
The help message goes here.
'''

maxInt = 100000
maxCharLength = 50

class Usage(Exception):
    def __init__(self, msg):
        self.msg = msg

class Schema:
    def __init__(self):
        self.tables = {}
        self.tables['U'] = ['varchar(50)', 'varchar(50)']
        self.tables['S'] = ['varchar(50)', 'varchar(50)']
        self.tables['R'] = ['varchar(50)', 'varchar(50)']
    
    def getTables(self):
        return self.tables
    

def generateTables(tables):
        tableData, cValueList = generateU(attributes = tables.get('U'))
        writeToFile(tableData, 'U.csv')

        tableData, b1ValueList = generateS(cList = cValueList, attributes = tables.get('S'))
        writeToFile(tableData, 'S1.csv')

        tableData, b2ValueList = generateS(cList = cValueList, attributes = tables.get('S'))
        writeToFile(tableData, 'S2.csv')

        tableData = generateR(bList = b1ValueList, attributes = tables.get('R'))
        writeToFile(tableData, 'R1.csv')
        
        tableData = generateR(bList = b1ValueList, attributes = tables.get('R'))
        writeToFile(tableData, 'R2.csv')
        
        tableData = generateR(bList = b2ValueList, attributes = tables.get('R'))
        writeToFile(tableData, 'R3.csv')
        
        tableData = generateR(bList = b2ValueList, attributes = tables.get('R'))
        writeToFile(tableData, 'R4.csv')
        
        tableData = generateErrMarkers()
        writeToFile(tableData, 'errMarkers.csv')
        

def writeToFile(lineList, fileName):
    outFile = file(fileName, 'w')
    lineList = map(lambda(x): str(x)+'\n', lineList)
    outFile.writelines(lineList)
    outFile.close()


def generateErrMarkers(numTuples=1000):
    data = []
    maxBitValue = 15 # 4 bits: 1111
    for tid in range(numTuples):
        attrValue = random.randint(1, maxBitValue)
        data.append(bin(attrValue)[2:])
    return data
    
    
def generateU(attributes, maxInt=100000, numTuples=100000, delimiter=','):
    '''This method needs to return both the data it generates and the list of values for cList
    for the foreign keys from table tramp and socialworker.'''
    data = []
    cList = []
    for tid in range(numTuples):
        line = str(tid) + ','
        i = 0 # index for cList
        for attr in attributes:
            attrValue = ''
            if attr.startswith('varchar'):
                attrLength = int(attr[8:-1])
                attrValue = ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(random.randint(1,attrLength)))
                if i == 0:
                    attrValue += str(tid) # primary key
                    cList.append(attrValue) # value list for foreign key
            elif attr.startswith('int'):
                attrValue = random.randint(1, maxInt)
            else:
                raise exception('datatype not supported: %s' % a)
            i += 1
            line += str(attrValue)+delimiter
        line = line[:-1]
        data.append(line)
    return data, cList
    

def generateS(attributes, cList, maxStrLength=10, maxInt=100000, numTuples=100000, delimiter=','):
    '''This method needs to return both the data it generates and the list of values for ssn
    for the foreign keys from table tramp.'''
    data = []
    bList = []
    for tid in range(numTuples):
        line = str(tid) + ','
        i = 0 # index for ssn's
        for attr in attributes:
            attrValue = ''
            if attr.startswith('varchar'):
                if i == 1: # The attribute for cList, pick one from cList list
                    attrValue = cList[random.randint(0, len(cList)-1)]
                else: # non-foreign keys
                    attrLength = int(attr[8:-1])
                    attrValue = ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(random.randint(1,attrLength)))
                    if i == 0: # Primary key and values for the foreign key
                        attrValue += str(tid)
                        bList.append(attrValue)
            elif attr.startswith('int'):
                attrValue = random.randint(1, maxInt)
                if i == 0:
                    attrValue = attrValue * (10 ** len(str(tid))) + tid # add tid to the end to make it unique
                    bList.append(attrValue)
            else:
                raise exception('datatype not supported: %s' % a)
            i += 1
            line += str(attrValue)+delimiter
        line = line[:-1]
        data.append(line)
    return data, bList
    

def generateR(attributes, bList, maxStrLength=10, maxInt=100000, numTuples=100000, delimiter=','):
    '''This method needs only to return the data'''
    data = []
    for tid in range(numTuples):
        line = str(tid) + ','
        i = 0 # index for ssn's
        for attr in attributes:
            attrValue = ''
            if attr.startswith('varchar'):
                if i == 1: # locations, pick one from locations list
                    attrValue = bList[random.randint(0, len(bList)-1)]
                else: # Not the attribute for locations
                    attrLength = int(attr[8:-1])
                    attrValue = ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(random.randint(1,attrLength)))
                    if i == 0: # primary key
                        attrValue += str(tid)
            else:
                raise exception('datatype not supported: %s' % a)
            i += 1
            line += str(attrValue)+delimiter
        line = line[:-1]
        data.append(line)
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
                
        schema = Schema()
        generateTables(schema.getTables())
    
    except Usage, err:
        print >> sys.stderr, sys.argv[0].split("/")[-1] + ": " + str(err.msg)
        print >> sys.stderr, "\t for help use --help"
        return 2




if __name__ == "__main__":
    sys.exit(main())
