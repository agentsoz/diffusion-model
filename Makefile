VERSION=diffusion-model-1.0.0-SNAPSHOT

QUICK=-DskipTests

full:
	mvn clean install

quick:
	mvn clean install ${QUICK}

target/${VERSION}/${VERSION}.jar :
	cd target && unzip -o ${VERSION}-bin.zip

