VENV := .venv
PIP := $(VENV)/bin/pip
ZENSICAL := $(VENV)/bin/zensical

$(VENV)/bin/pip:
	python3 -m venv $(VENV)

.PHONY: docs-deps
docs-deps: $(VENV)/bin/pip
	$(PIP) install -r .github/docs-requirements.txt

.PHONY: docs-build
docs-build: docs-deps
	$(ZENSICAL) build

.PHONY: docs-serve
docs-serve: docs-deps
	$(ZENSICAL) serve
