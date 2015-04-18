import syntaxtree.*;
import visitor.*;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import java.util.List;
import java.util.ArrayList;

import java_cup.runtime.*;

public class MiniJavaCodeGen {
    
    protected Tree symbolTable = null;
    protected ArrayList<Quadruple> IR = null;
    
    private ArrayList<String> args = new ArrayList<>();
    private int argsCounter = 0;
    
    private ArrayList<String> genRegs = new ArrayList<>();
    private int regsCounter = 0;
    
    private Node curScope;
    
    public MiniJavaCodeGen(Tree symbol, ArrayList<Quadruple> IR) {
        this.symbolTable = symbol;
        this.IR = IR;
        this.curScope = this.symbolTable.getRoot();
        for(int k = 0; k <= 3; k++){
            args.add("$a" + k);
        }
        for(int k = 0; k <= 9; k++){
            genRegs.add("$t" + k);
        }
        for(int k = 0; k <= 7; k++){
            genRegs.add("$s" + k);
        }
        genRegs.add("$v1");
        genRegs.add("$at");
        genRegs.add("$gp");
    }
    
    public void generate(){
        PrintWriter writer = null;
        try{
            writer = new PrintWriter("generated.asm");
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        writer.println(".text");
        for(Quadruple q: IR){
            if(q.op.equals(":=")){
                if(q.arg1 instanceof IntegerLiteral){
                    IntegerLiteral il = (IntegerLiteral) q.arg1;
                    Node node = checkOrAdd(q.result.toString());
                    writer.println("li " + node.getData().get(q.result.toString()).get(1) + " " + il.i);
                }
                else{
                    Node node1 = checkOrAdd(q.result.toString());
                    Node node2 = checkOrAdd(q.arg1.toString());
                    writer.println("move " + node1.getData().get(q.result.toString()).get(1) + " " + node2.getData().get(q.arg1.toString()).get(1));
                }
            }
            else if(q.op.equals("PARAM")){
                if(q.result instanceof IntegerLiteral){
                    IntegerLiteral il = (IntegerLiteral) q.result;
                    writer.println("li " + args.get(argsCounter++) + " " + il.i);
                }
                else if(q.result instanceof String){
                    String temp = q.result.toString();
                    Node node = checkOrAdd(temp);
                    writer.println("move " + args.get(argsCounter++) + " " + node.getData().get(temp).get(1));
                }
            }
            else if(q.op.equals("LABEL")){
                writer.println(q.result);
            }
            else if(q.op.equals("CALL")){
                writer.println("jal " + q.arg1);
                if(q.result != null){
                    Node node = checkOrAdd(q.result.toString());
                    writer.println("move " + node.getData().get(q.result.toString()).get(1) + " $v0");
                }
                String result = q.arg1.toString();
                if(!result.equals("_system_out_println") && !result.equals("_system_exit") && !result.equals("_new_object") && !result.equals("_new_array")){
                    //System.err.println("Scope should be " + result);
                    this.curScope = symbolTable.search(result.substring(0, result.length()), symbolTable.getRoot());
                    for(int k = 0; k < curScope.getParams().size(); k++){
                        curScope.getData().get(curScope.getParams().get(k)).add(args.get(k));
                        //System.err.println("Matching " + curScope.getParams().get(k) + " to " + args.get(k));
                    }
                    //if(this.curScope == null) System.err.println("Scope is null");
                    //else System.err.println("Scope is " + curScope.getName());
                }
                argsCounter = 0;
            }
            else if(q.op.equals("RETURN")){
                if(q.result instanceof IntegerLiteral){
                    IntegerLiteral il = (IntegerLiteral) q.result;
                    writer.println("li $v0 " + il.i);
                }
                else{
                    //Node node = checkOrAdd(q.result.toString());
                    writer.println("move $v0 " + curScope.getData().get(q.result.toString()).get(1));
                    
                }
                writer.println("jr $ra");
            }
            else if(q.op.equals("&&")) {
                if(q.arg1 instanceof IntegerLiteral && q.arg2 instanceof IntegerLiteral){
                    IntegerLiteral il1 = (IntegerLiteral) q.arg1;
                    IntegerLiteral il2 = (IntegerLiteral) q.arg2;
                    writer.println("li " + genRegs.get(regsCounter) + " " + il1.i);
                    writer.println("andi " + genRegs.get(regsCounter) + " " + genRegs.get(regsCounter) + " " + il2.i);
                    Node node = checkOrAdd(q.result.toString());
                }
                else if(q.arg2 instanceof IntegerLiteral){
                    Node node1 = checkOrAdd(q.arg1.toString());
                    Node node2 = checkOrAdd(q.result.toString());
                    IntegerLiteral il = (IntegerLiteral) q.arg2;
                    writer.println("andi " + node2.getData().get(q.result.toString()).get(1) + " " + node1.getData().get(q.arg1.toString()).get(1) + " " + il.i);
                }
                else if(q.arg1 instanceof IntegerLiteral){
                    Node node1 = checkOrAdd(q.arg2.toString());
                    Node node2 = checkOrAdd(q.result.toString());
                    IntegerLiteral il = (IntegerLiteral) q.arg1;
                    writer.println("andi " + node2.getData().get(q.result.toString()).get(1) + " " + node1.getData().get(q.arg2.toString()).get(1) + " " + il.i);
                }
                else{
                    Node node1 = checkOrAdd(q.arg1.toString());
                    Node node2 = checkOrAdd(q.result.toString());
                    Node node3 = checkOrAdd(q.arg2.toString());
                    writer.println("and " + node2.getData().get(q.result.toString()).get(1) + " " + node1.getData().get(q.arg1.toString()).get(1) + " " + node3.getData().get(q.arg2.toString()).get(1));
                }
            }
            else if(q.op.equals("<")){
                if(q.arg1 instanceof IntegerLiteral && q.arg2 instanceof IntegerLiteral){
                    IntegerLiteral il1 = (IntegerLiteral) q.arg1;
                    IntegerLiteral il2 = (IntegerLiteral) q.arg2;
                    writer.println("li " + genRegs.get(regsCounter) + " " + il1.i);
                    writer.println("slti " + genRegs.get(regsCounter) + " " + genRegs.get(regsCounter) + " " + il2.i);
                    //System.err.println(q.result.toString());
                    Node node = checkOrAdd(q.result.toString());
                }
                else if(q.arg2 instanceof IntegerLiteral){
                    Node node1 = checkOrAdd(q.arg1.toString());
                    Node node2 = checkOrAdd(q.result.toString());
                    IntegerLiteral il = (IntegerLiteral) q.arg2;
                    writer.println("slti " + node2.getData().get(q.result.toString()).get(1) + " " + node1.getData().get(q.arg1.toString()).get(1) + " " + il.i);
                }
                else if(q.arg1 instanceof IntegerLiteral){
                    Node node1 = checkOrAdd(q.arg2.toString());
                    Node node2 = checkOrAdd(q.result.toString());
                    IntegerLiteral il = (IntegerLiteral) q.arg1;
                    writer.println("li " + genRegs.get(regsCounter) + " " + il.i);
                    writer.println("slt " + node2.getData().get(q.result.toString()).get(1) + " " + genRegs.get(regsCounter) + " " + node1.getData().get(q.arg2.toString()).get(1));
                }
                else{
                    Node node1 = checkOrAdd(q.arg1.toString());
                    Node node2 = checkOrAdd(q.result.toString());
                    Node node3 = checkOrAdd(q.arg2.toString());
                    writer.println("slt " + node2.getData().get(q.result.toString()).get(1) + " " + node1.getData().get(q.arg1.toString()).get(1) + " " + node3.getData().get(q.arg2.toString()).get(1));
                }
            }
            else if(q.op.equals("+")){
                if(q.arg1 instanceof IntegerLiteral && q.arg2 instanceof IntegerLiteral){
                    IntegerLiteral il1 = (IntegerLiteral) q.arg1;
                    IntegerLiteral il2 = (IntegerLiteral) q.arg2;
                    writer.println("li " + genRegs.get(regsCounter) + " " + il1.i);
                    writer.println("addi " + genRegs.get(regsCounter) + " " + genRegs.get(regsCounter) + " " + il2.i);
                    //System.err.println(q.result.toString());
                    Node node = checkOrAdd(q.result.toString());
                }
                else if(q.arg2 instanceof IntegerLiteral){
                    Node node1 = checkOrAdd(q.arg1.toString());
                    Node node2 = checkOrAdd(q.result.toString());
                    IntegerLiteral il = (IntegerLiteral) q.arg2;
                    writer.println("addi " + node2.getData().get(q.result.toString()).get(1) + " " + node1.getData().get(q.arg1.toString()).get(1) + " " + il.i);
                }
                else if(q.arg1 instanceof IntegerLiteral){
                    Node node1 = checkOrAdd(q.arg2.toString());
                    Node node2 = checkOrAdd(q.result.toString());
                    IntegerLiteral il = (IntegerLiteral) q.arg1;
                    writer.println("addi " + node2.getData().get(q.result.toString()).get(1) + " " + node1.getData().get(q.arg2.toString()).get(1) + " " + il.i);
                }
                else{
                    Node node1 = checkOrAdd(q.arg1.toString());
                    Node node2 = checkOrAdd(q.result.toString());
                    Node node3 = checkOrAdd(q.arg2.toString());
                    writer.println("add " + node2.getData().get(q.result.toString()).get(1) + " " + node1.getData().get(q.arg1.toString()).get(1) + " " + node3.getData().get(q.arg2.toString()).get(1));
                }
            }
            else if(q.op.equals("-")){
                if(q.arg1 instanceof IntegerLiteral && q.arg2 instanceof IntegerLiteral){
                    IntegerLiteral il1 = (IntegerLiteral) q.arg1;
                    IntegerLiteral il2 = (IntegerLiteral) q.arg2;
                    writer.println("li " + genRegs.get(regsCounter) + " " + il1.i);
                    writer.println("addi " + genRegs.get(regsCounter) + " " + genRegs.get(regsCounter) + " -" + il2.i);
                    Node node = checkOrAdd(q.result.toString());
                }
                else if(q.arg2 instanceof IntegerLiteral){
                    Node node1 = checkOrAdd(q.arg1.toString());
                    Node node2 = checkOrAdd(q.result.toString());
                    IntegerLiteral il = (IntegerLiteral) q.arg2;
                    writer.println("addi " + node2.getData().get(q.result.toString()).get(1) + " " + node1.getData().get(q.arg1.toString()).get(1) + " -" + il.i);
                }
                else if(q.arg1 instanceof IntegerLiteral){
                    Node node1 = checkOrAdd(q.arg2.toString());
                    Node node2 = checkOrAdd(q.result.toString());
                    IntegerLiteral il = (IntegerLiteral) q.arg1;
                    writer.println("li " + genRegs.get(regsCounter) + " " + il.i);
                    writer.println("sub " + node2.getData().get(q.result.toString()).get(1) + " " + genRegs.get(regsCounter) + " " + node1.getData().get(q.arg2.toString()).get(1));
                }
                else{
                    Node node1 = checkOrAdd(q.arg1.toString());
                    Node node2 = checkOrAdd(q.result.toString());
                    Node node3 = checkOrAdd(q.arg2.toString());
                    writer.println("sub " + node2.getData().get(q.result.toString()).get(1) + " " + node1.getData().get(q.arg1.toString()).get(1) + " " + node3.getData().get(q.arg2.toString()).get(1));
                }
            }
            else if(q.op.equals("*")){
                if(q.arg1 instanceof IntegerLiteral && q.arg2 instanceof IntegerLiteral){
                    IntegerLiteral il1 = (IntegerLiteral) q.arg1;
                    IntegerLiteral il2 = (IntegerLiteral) q.arg2;
                    writer.println("li " + genRegs.get(regsCounter) + " " + il1.i);
                    writer.println("li " + genRegs.get(regsCounter+1) + " " + il2.i);
                    writer.println("mult " + genRegs.get(regsCounter) + " " + genRegs.get(regsCounter+1));
                    writer.println("mflo " + genRegs.get(regsCounter));
                    Node node = checkOrAdd(q.result.toString());
                }
            }
            else if(q.op.equals("!")) {
            
            } 
            else if(q.op.equals("NEW")) {
            
            }
            else if(q.op.equals("length")){
            
            } 
            else if(q.op.equals("=[]")) {
            
            } 
            else if(q.op.equals("[]=")) {
            
            } 
            else if(q.op.equals("IFFALSE")) {
                Node node = checkOrAdd(q.arg1.toString());
                writer.println("beq " + node.getData().get(q.arg1.toString()).get(1) + " $zero " + q.result);
            } 
            else if(q.op.equals("GOTO")) {
                writer.println("j " + q.result.toString());
            }
            else if(q.op.equals("END MAIN")){
                writer.println("jal _system_exit");
            }
            
        
        }
        writer.println();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader("runtime.asm"));
            while(reader.ready()){
                writer.println(reader.readLine());
            } reader.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        writer.close();
    }
    
    private Node checkOrAdd(String reg){
        Node node = curScope.parentsSearch(reg);
        if(node == null){
            curScope.getData().put(reg, new ArrayList<String>());
            curScope.getData().get(reg).add("");
            curScope.getData().get(reg).add(genRegs.get(regsCounter++));
            node = curScope;
        }
        else{
            if(node.getData().get(reg).size() < 2) node.getData().get(reg).add(genRegs.get(regsCounter++));
        }
        return node;
    }


}