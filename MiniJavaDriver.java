import syntaxtree.*;
import visitor.*;

import java.io.FileReader;
import java.io.IOException;

import java_cup.runtime.*;

public class MiniJavaDriver{

    public static void main(String[] args){
        
        if(args.length != 1){
            System.err.println("Usage: java MiniJavaDriver miniJava.java");
            System.exit(1);
        }
        Symbol parse_tree = null;
        try{
            MiniJavaParser parser = new MiniJavaParser(new MiniJavaLexer(new FileReader(args[0])));

            parse_tree = parser.parse();
            Program root = (Program) parse_tree.value;
            root.accept(new PrettyPrintVisitor());
        }
        catch(IOException e){
            System.err.println("Unable to open file: " + args[0]);
        }
        catch(Exception e){
            e.printStackTrace(System.err);
        }
    }
}