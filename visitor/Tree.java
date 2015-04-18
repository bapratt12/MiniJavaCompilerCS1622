package visitor;

import java.util.HashMap;
import java.util.ArrayList;

public class Tree {
    private Node root;

    public Tree(HashMap<String, ArrayList<String>> rootData) {
        root = new Node("root");
        root.data = rootData;
        root.children = new ArrayList<Node>();
    }
	
	public Node getRoot(){
		return root;
	}
	
	protected void setRootName(String rootName){
		root = new Node(rootName);
	}
	
	public Node search(String name, Node node){
		if(node != null){
			if(node.getName().equals(name)){
				return node;
			}
			else{
				Node foundNode = null;
				for(Node n: node.getChildren()){
					foundNode = search(name, n);
					if(foundNode != null){
						break;
					}
				}
				return foundNode;
			}
		}
		else{
			return null;
		}	
	}
}