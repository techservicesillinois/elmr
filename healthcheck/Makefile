BUILD := healthcheck
DEPS := ajpclient.go

export CGO_ENABLED=0

all: $(BUILD)

deps:
	go get -d -v

compress: $(BUILD)
	upx -q --brute $(BUILD)
	upx -t $(BUILD)

test: $(BUILD)
	./$(BUILD) http://localhost:8009/auth/elmr/config
	./$(BUILD) -c 200 http://localhost:8009/auth/elmr/config
	./$(BUILD) -c 302 http://localhost:8009/auth/elmr/attributes
	./$(BUILD) -c 404 http://localhost:8009
	
	./$(BUILD) -c 302 http://localhost:8009/auth/elmr http://localhost:8009/auth/elmr
	./$(BUILD) -c 200 http://localhost:8009/auth/elmr/config -c 302 http://localhost:8009/auth/elmr
	
	! ./$(BUILD) -c 200 http://localhost:8009/auth/elmr/config -c 302
	! ./$(BUILD) -c 200 http://localhost:8009/auth/elmr/config -c 302 http://localhost:8009/auth/elmr/config http://localhost:8009/auth/elmr/config

$(BUILD): $(DEPS)
	go build -ldflags "-w -s" -a -installsuffix cgo -o $@

clean:
	rm -f $(BUILD)
