JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	lib/*.java \
	server/*.java \
	client/*.java \

runServer:
	java server.RMIServer 1097 1098 1198 1234
runClient:
	java client.RMIClient 1097 1098 1198 1234
	
default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) lib/*.class
	$(RM) server/*.class
	$(RM) client/*.class