.PHONY: all base login push pull clean

SRCS := Dockerfile target/elmr-distribution.tar.gz
IMAGE:= techservicesillinois/elmr

all: base .drone.yml.sig

base: .base
.base: $(SRCS)
	docker build -f Dockerfile -t $(IMAGE) .
	@touch $@

target/elmr-distribution.tar.gz:
	mvn package

login:
	docker login

push: base 
	docker push $(IMAGE)

pull:
	docker pull $(IMAGE)

.drone.yml.sig: .drone.yml
	drone sign cites-illinois/elmr
	git add $^ $@

clean:
	-docker rmi $(IMAGE)
	-rm -f .base 
	-mvn clean
