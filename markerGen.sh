#!/bin/bash

OUTDIR=/home/perm/TrampExGen/resource/test/markers/
JARDIR=/home/perm/TrampExGen/build/macexplgen/
GENDIR=/home/perm/2015_VagaExpData/datasize
LOADIR=/home/perm/TrampExGen/build/macloader/

noReuse=100
numAttr=1000

mkdir -p ${OUTDIR}
java -XX:-UseGCOverheadLimit -jar ${LOADIR}/loadermac.jar -f ~/scenarios/CP${numAttr}_I1000_NR${noReuse}_SR100_TR0/CP${numAttr}_I1000_NR${noReuse}_SR100_TR0.xml -u postgres -d postgres
echo "-------------------scenario loaded------------------------"

ant
for numMrk in 1 5 50 100
do
	java -cp ${JARDIR}/explmac.jar org.vagabond.performance.bitmarker.TestMarkerGenerator ${numMrk} > ${OUTDIR}/CP${numAttr}_markers_${numMrk}EM.txt
	echo "-------------------------"${numAttr}" Marker Generated-----------------------"
	wc -l < ${OUTDIR}/CP${numAttr}_markers_${numMrk}EM.txt

	java -XX:-UseGCOverheadLimit -jar ${JARDIR}/explmac.jar -x ~/scenarios/CP${numAttr}_I1000_NR${noReuse}_SR100_TR0/CP${numAttr}_I1000_NR${noReuse}_SR100_TR0.xml -m ${OUTDIR}/CP${numAttr}_markers_${numMrk}EM.txt -u postgres -d postgres -nonInteractive -noShowCES > ${GENDIR}/CP${numAttr}_I1000_NR${noReuse}_SR100_TR0_EM${numMrk}.txt
	echo "-------------------------------CP"${numAttr}" Row Data Generated--------------------------------"
	wc -l < ${GENDIR}/CP${numAttr}_I1000_NR${noReuse}_SR100_TR0_EM${numMrk}.txt

	echo "-------------------------------"${numMrk}" Avg time-------------------------------"
	awk '{ total += $2; count++ } END { print total/count }'  ${GENDIR}/CP${numAttr}_I1000_NR${noReuse}_SR100_TR0_EM${numMrk}.txt

	echo \\
done




