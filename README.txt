
Build with 
make -f Makefile

Run with
java -cp "java-cup-11a.jar;." MiniJavaDriver miniJavaFile.java


Lexer and Parser .java files are provided, if for some reason they are missing they can be made with
java -jar java-cup-11a.jar -interface -parser MiniJavaParser MiniJava.cup
jflex -d . MiniJava.flex

If make does not work, compile with
javac -cp "java-cup-11a.jar;." MiniJavaDriver.java

OPTIMIZATION:
-Algebraic Simplification
	x = 0 * y => x = 0
	x = 1 * y => x = y
	x = y + 0 || x = y - 0 => x = y
-Copy Propagation
	Whenever a binary operation is encountered with both operands being constants, the value is computed at compile time.
	The result of this statement is remembered; let's call it x.
	All following statements until the next definition of x that use x as an argument are updated to the computed value.
	
	DISCLAIMER
		This only works in some cases.
		It will work in the included Milestone10.java, but if line 20 were removed, the while loop would be incorrect.

KNOWN BUGS:
For error checking
	All name/type checking is implemented except for method argument type/length
	Tabs treated as 1 character, making line and character off in lines with tabs (tabs as spaces will not have this problem)
	If a method is used in a class before it has been declared in the program, an "Unidentified identifier" error will be output even though the method exists
