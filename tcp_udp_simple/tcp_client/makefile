JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
        Client.java \
        ClientTCP.java \
        Logger.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class