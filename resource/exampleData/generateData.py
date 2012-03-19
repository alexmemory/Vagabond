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
        self.tables['tramp'] = ['varchar(50)', 'varchar(50)', 'varchar(50)', 'int']
        self.tables['socialworker'] = ['int', 'varchar(50)', 'varchar(50)']
        self.tables['soupkitchen'] = ['varchar(50)', 'varchar(50)', 'int']
    
    def getTables(self):
        return self.tables
    
def generateTables(tables, maxInteger=100000, cardinality=100000):
        tableData, locs = generateSoupKitchen(attributes = tables.get('soupkitchen'), maxInt = maxInteger, numTuples = cardinality)
        writeToFile(tableData, 'soupkitchen.csv')

        tableData, ssns = generateSocialWorker(locations = locs, attributes = tables.get('socialworker'), maxInt = maxInteger, numTuples = cardinality)
        writeToFile(tableData, 'socialworker.csv')

        tableData = generateTramp(locations = locs, ssnList = ssns, attributes = tables.get('tramp'), maxInt = maxInteger, numTuples = cardinality)
        writeToFile(tableData, 'tramp.csv')
        

def writeToFile(lineList, fileName):
    outFile = file(fileName, 'w')
    lineList = map(lambda(x): str(x)+'\n', lineList)
    outFile.writelines(lineList)
    outFile.close()
    

def generateSoupKitchen(attributes, maxInt=100000, numTuples=100000, delimiter=','):
    '''This method needs to return both the data it generates and the list of values for locations
    for the foreign keys from table tramp and socialworker.'''
    data = []
    locations = []
    for tid in range(numTuples):
        line = str(tid) + ','
        i = 0 # index for locations
        for attr in attributes:
            attrValue = ''
            if attr.startswith('varchar'):
                attrLength = int(attr[8:-1])
                attrValue = ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(random.randint(1,attrLength)))
                if i == 0:
                    attrValue += str(tid) # primary key
                    locations.append(attrValue)
            elif attr.startswith('int'):
                attrValue = random.randint(1, maxInt)
            else:
                raise exception('datatype not supported: %s' % a)
            i += 1
            line += str(attrValue)+delimiter
        line = line[:-1]
        data.append(line)
    return data, locations
    

def generateSocialWorker(attributes, locations, maxStrLength=10, maxInt=100000, numTuples=100000, delimiter=','):
    '''This method needs to return both the data it generates and the list of values for ssn
    for the foreign keys from table tramp.'''
    data = []
    ssns = []
    for tid in range(numTuples):
        line = str(tid) + ','
        i = 0 # index for ssn's
        for attr in attributes:
            attrValue = ''
            if attr.startswith('varchar'):
                if i == 2: # The attribute for locations, pick one from locations list
                    attrValue = locations[random.randint(0, len(locations)-1)]
                else: # non-locations
                    attrLength = int(attr[8:-1])
                    attrValue = ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(random.randint(1,attrLength)))
            elif attr.startswith('int'):
                attrValue = random.randint(1, maxInt)
                if i == 0:
                    attrValue = attrValue * (10 ** len(str(tid))) + tid # add tid to the end to make it unique
                    ssns.append(attrValue)
            else:
                raise exception('datatype not supported: %s' % a)
            i += 1
            line += str(attrValue)+delimiter
        line = line[:-1]
        data.append(line)
    return data, ssns
    

def generateTramp(attributes, locations, ssnList, maxStrLength=10, maxInt=100000, numTuples=100000, delimiter=','):
    '''This method needs only to return the data'''
    data = []
    for tid in range(numTuples):
        line = str(tid) + ','
        i = 0 # index for ssn's
        for attr in attributes:
            attrValue = ''
            if attr.startswith('varchar'):
                if i == 2: # locations, pick one from locations list
                    attrValue = locations[random.randint(0, len(locations)-1)]
                else: # Not the attribute for locations
                    attrLength = int(attr[8:-1])
                    attrValue = ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(random.randint(1,attrLength)))
                    if i == 0: # primary key
                        attrValue += str(tid)
            elif attr.startswith('int'):
                if i == 3: # ssn
                    attrValue = ssnList[random.randint(0, len(ssnList)-1)]
                else:
                    attrValue = random.randint(1, maxInt)
            else:
                raise exception('datatype not supported: %s' % a)
            i += 1
            line += str(attrValue)+delimiter
        line = line[:-1]
        data.append(line)
    return data



def main(argv=None):
    cardinality = 100000
    if argv is None:
        argv = sys.argv
    try:
        try:
            opts, args = getopt.getopt(argv[1:], "hoc:v", ["help", "output=", "cardinality="])
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
            if option in ("-c", "--cardinality"):
                cardinality = int(value)
                
        schema = Schema()
        generateTables(schema.getTables(), cardinality = cardinality)
    
    except Usage, err:
        print >> sys.stderr, sys.argv[0].split("/")[-1] + ": " + str(err.msg)
        print >> sys.stderr, "\t for help use --help"
        return 2




if __name__ == "__main__":
    sys.exit(main())
