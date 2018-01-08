package cop5556fa17;

import java.util.HashMap;

import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.*;
import cop5556fa17.TypeCheckVisitor.SemanticException;

public class SymbolTable {
	
	HashMap<String, ASTNode> symbolTable = new HashMap<String, ASTNode>();
	
	public void insert(String name, ASTNode node) throws SemanticException {
		if(symbolTable.containsKey(name))
		{
			throw new SemanticException(node.firstToken, "symbol table already contains a key");
		}
		else {
		symbolTable.put(name, node);
		
	}}
	
	public Type lookupType(String name)
	{
		Type t = null;
	
		if(symbolTable.containsKey(name)) {
		ASTNode node = symbolTable.get(name);
		 t = TypeUtils.getType(node.firstToken); }
		
		return t;
	}

	public Declaration getDec(String name) {
		// TODO Auto-generated method stub
		Declaration d = null;
		if(symbolTable.containsKey(name)) {
		d=  (Declaration) symbolTable.get(name);
	}
		return d;
	}}
