.PHONY: all image login push pull clean

# http://blog.jgc.org/2011/07/gnu-make-recursive-wildcard-function.html
rwildcard=$(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

JAR  := target/elmr-distribution.tar.gz
JSRC := pom.xml $(call rwildcard,src,*)

SRCS := Dockerfile $(JSRC) attribute-map.xml manifest
IMAGE:= techservicesillinois/elmr

all: image .drone.yml.sig

image: .image
.image: $(SRCS)
	docker build -f Dockerfile -t $(IMAGE) .
	@touch $@

login:
	docker login

push: .push
.push: image
	docker push $(IMAGE)
	@touch $@

pull:
	docker pull $(IMAGE)

.drone.yml.sig: .drone.yml
	drone sign cites-illinois/elmr
	git add $^ $@

clean:
	-docker rmi $(IMAGE)
	-rm -f .image .push
