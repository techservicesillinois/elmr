.PHONY: all base login push pull clean

# http://blog.jgc.org/2011/07/gnu-make-recursive-wildcard-function.html
rwildcard=$(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

JAR  := target/elmr-distribution.tar.gz
JSRC := $(call rwildcard,src,*)

SRCS := Dockerfile $(JAR)
IMAGE:= techservicesillinois/elmr

all: base .drone.yml.sig

base: .base
.base: $(SRCS)
	docker build -f Dockerfile -t $(IMAGE) .
	@touch $@

$(JAR): $(JSRC)
	mvn package

login:
	docker login

push: .push
.push: base
	docker push $(IMAGE)
	@touch $@

pull:
	docker pull $(IMAGE)

.drone.yml.sig: .drone.yml
	drone sign cites-illinois/elmr
	git add $^ $@

clean:
	-docker rmi $(IMAGE)
	-rm -f .base 
	-mvn clean
