all: run

package:
	mvn -q clean package

run: package
	mvn -q -e exec:java -D exec.mainClass=randsampFM.TestMain
