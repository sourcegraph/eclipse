all:
	./scripts/build-all.sh
format:
	python scripts/format.py
	python scripts/format.py # need to seemingly run twice
