all:
	./scripts/build-agent.sh
	./scripts/build-webview.sh
	./scripts/download-node.sh
bindings:
	./scripts/build-java-bindings.sh
format:
	python scripts/format.py
	python scripts/format.py # need to seemingly run twice
