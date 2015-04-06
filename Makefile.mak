JAVA=java
JAVAC=javac -cp "java-cup-11a.jar;."
JFLEX=jflex -d .
CUP=$(JAVA) -jar java-cup-11a.jar -interface -expect 20 -parser MiniJavaParser

all: MiniJavaDriver.java MiniJavaLexer.java MiniJavaParser.java
	$(JAVAC) MiniJavaDriver.java

MiniJavaLexer.java: MiniJava.flex
	$(JFLEX) MiniJava.flex

MiniJavaParser.java: MiniJava.cup ./syntaxTree/*.java
	$(CUP) MiniJava.cup