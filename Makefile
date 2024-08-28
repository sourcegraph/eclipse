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
	python scripts/download-eclipse.py
