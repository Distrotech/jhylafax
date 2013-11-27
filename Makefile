JAR = net.sf.jhylafax-1.4.0-jar-with-dependencies.jar
TARGET = target/$(JAR)

all: $(TARGET)

$(TARGET):
	mvn assembly:assembly
	
clean:
	mvn clean

install: all
	install -d $(DESTDIR)/usr/share/java
	install -m 644 -t $(DESTDIR)/usr/share/java $(TARGET)
	rm -f $(DESTDIR)/usr/share/java/jhylafax.jar
	ln -s $(JAR) $(DESTDIR)/usr/share/java/jhylafax.jar
