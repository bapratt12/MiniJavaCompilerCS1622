
Build with 
make -f Makefile

Run with
java -cp "java-cup-11a.jar;." MiniJavaDriver miniJavaFile.java


Lexer and Parser .java files are provided, if for some reason they are missing they can be made with
java -jar java-cup-11a.jar -interface -parser MiniJavaParser MiniJava.cup
jflex -d . MiniJava.flex

If make does not work, compile with
javac -cp "java-cup-11a.jar;." MiniJavaDriver.java






KNOWN BUGS:
The only error I found was with distinguishing between negative numbers and subtraction with no spacing. For example
4-1
I can't find a way to determine if this is 4 minus 1 or a 4 followed by a -1.
Standalone negative numbers will work, as will subtraction with spacing, so
int i;
i = -2;
i = 4 - 1;
will work.