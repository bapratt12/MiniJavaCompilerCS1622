package visitor;

import java.util.HashMap;
import java.util.ArrayList;

public class Node {
	protected String name;
	protected String type;
    protected HashMap<String, String> data;
    protected Node parent;
    protected ArrayList<Node> children;
		
    public Node(String nodeName){
		name = nodeName;
		type = null;
		data = new HashMap<String, String>();
		parent = null;
		children = new ArrayList<Node>();
	}
		
	public Node(String nodeName, Node nodeParent){
		name = nodeName;
		type = null;
		data = new HashMap<String, String>();
		parent = nodeParent;
		children = new ArrayList<Node>();
	}
	
	public Node(String nodeName, String nodeType, Node nodeParent){
		name = nodeName;
		type = nodeType;
		data = new HashMap<String, String>();
		parent = nodeParent;
		children = new ArrayList<Node>();
	}
		
	public HashMap<String, String> getData(){
		return data;
	}
	
	public String getName(){
		return name;
	}
	
	public String getType(){
		return type;
	}
	
	public Node getParent(){
		return parent;
	}
		
	public ArrayList<Node> getChildren(){
		return children;
	}
	
	public boolean addChild(Node child){
		return children.add(child);
	}
	
	//search node and its parents for a key
	public boolean parentsContains(String str){
		Node temp = this;
		do{
			if(temp.getData().containsKey(str)){
				return true;
			}
			temp = temp.parent;
		}while(temp !=null);
		return false;
		
	}
	
	public Node parentsSearch(String str){
		Node temp = this;
		do{
			if(temp.getData().containsKey(str)){
				return temp;
			}
			temp = temp.parent;
		}while(temp !=null);
		return null;
		
	}
  }