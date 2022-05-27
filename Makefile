all: run

package:
	mvn -q clean package

run: package
	mvn -q -e exec:java -D exec.mainClass=randsampFM.TestMain



compile:
	mvn -q clean compile assembly:single


newrun: compile
	java -ea -jar target/randsampFM-0.0.1-SNAPSHOT-jar-with-dependencies.jar
