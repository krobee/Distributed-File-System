all: compile
	@echo -e '[INFO] Done!\n' 
	
clean:
	@echo -e '\n[INFO] Cleaning Up..'
	@-rm -rf bin

compile: clean
	@-mkdir bin
	@-cp Data/* bin/
	@echo -e '[INFO] Compiling the Source..'
	@javac -d bin src/**/*.java


