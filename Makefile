CODY_COMMIT := $(shell cat ./plugins/cody-chat/cody-commit.txt)

all:
	./scripts/build-agent.sh
	./scripts/build-webview.sh
	./scripts/download-node.sh
build-bindings:
	./scripts/build-java-bindings.sh
format:
	python scripts/format.py
	python scripts/format.py # need to seemingly run twice
bindings: build-bindings format
clean:
	./scripts/clean.sh
prepare-ci:
	git clone https://github.com/sourcegraph/cody ../cody
	pushd ../cody && git checkout "$(CODY_COMMIT)" && popd 
	python scripts/download-eclipse.py
