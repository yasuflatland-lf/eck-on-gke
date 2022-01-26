.PHONY: clean
clean: ## Cleanup database
	rm -fR ./es/data
	mkdir ./es/data

.PHONY: destroy
destroy: ## Destroy all environment
	docker-compose down --rmi all --volumes --remove-orphans; \
	rm -fR ./es/data

.PHONY: run
run: ## Shorthand of running docker images
	ELASIC_VERSION=7.16.2 docker-compose up --build --remove-orphans

.PHONY: help
help: ## Display this help screen
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
