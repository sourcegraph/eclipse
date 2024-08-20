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
	rm -r plugins/cody-chat/resources/dist/
	rm -r ~/AppData/Roaming/Sourcegraph/CodyEclipse
	rm -r ~/AppData/Roaming/Cody-nodejs/Config/dist/webviews
