# Override when the default python3 cannot create venvs,
# e.g. make PYTHON=/path/to/python3.12 docs-serve
PYTHON ?= python3
VENV := .venv
PIP := $(VENV)/bin/pip
ZENSICAL := $(VENV)/bin/zensical

$(VENV)/bin/pip:
	$(PYTHON) -m venv $(VENV)

.PHONY: docs-deps
docs-deps: $(VENV)/bin/pip
	$(PIP) install -r .github/docs-requirements.txt

.PHONY: docs-build
docs-build: docs-deps
	$(ZENSICAL) build

.PHONY: docs-serve
docs-serve: docs-deps
	$(ZENSICAL) serve
