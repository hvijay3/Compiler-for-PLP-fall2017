package cop5556fa17;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.*;

public class TypeCheckVisitor implements ASTVisitor {
	 SymbolTable symbolTable = new SymbolTable();

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super("line " + t.line + " pos " + t.pos_in_line + ": " + message);
			this.t = t;
		}

	}

	/**
	 * The program name is only used for naming the class. It does not rule out
	 * variables with the same name. It is returned for convenience.
	 * 
	 * @throws Exception
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node : program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	/*
	 * REQUIRE: symbolTable.lookupType(name) = symbolTable.insert(name,
	 * Declaration_Image) Declaration_Variable.Type <= Type
	 */
	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		String name = declaration_Variable.name;
		Type t = symbolTable.lookupType(name); // it should not have been declared
		if (t != null) {
			throw new SemanticException(declaration_Variable.firstToken, name);
		}
		Expression e = declaration_Variable.e;

		if (e != null) {
			Type exprType = (Type) e.visit(this, null);
			symbolTable.insert(name, declaration_Variable);
			declaration_Variable.typeVal = symbolTable.lookupType(name);
			t = declaration_Variable.typeVal;

			if (t != exprType) {
				throw new SemanticException(declaration_Variable.firstToken, name);
			}
			return t;
		}
		symbolTable.insert(name, declaration_Variable);
		declaration_Variable.typeVal = symbolTable.lookupType(name);
		t = declaration_Variable.typeVal;
		return t;

	}

	/*
	 * Expression_Binary ::= Expression0 op Expression1 REQUIRE: Expression0.Type ==
	 * Expression1.Type && Expression_Binary.Type Expression_Binary.type <= if op ∈
	 * {EQ, NEQ} then BOOLEAN else if (op ∈ {GE, GT, LT, LE} && Expression0.Type ==
	 * INTEGER) then BOOLEAN else if (op ∈ {AND, OR}) && (Expression0.Type ==
	 * INTEGER || Expression0.Type ==BOOLEAN) then Expression0.Type else if op ∈
	 * {DIV, MINUS, MOD, PLUS, POWER, TIMES} && Expression0.Type == INTEGER then
	 * INTEGER else Ʇ
	 */
	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) 
			throws Exception {
		Expression e0 = expression_Binary.e0;
		Expression e1 = expression_Binary.e1;
		e0.typeVal = (Type) e0.visit(this, null);
		e1.typeVal = (Type) e1.visit(this, null);
		Type e0Type = e0.typeVal;
		Type e1Type = e1.typeVal;
		// expression_binary !=

		Type typeExprBinary;
		Kind op = expression_Binary.op;
		if (op == Kind.OP_EQ || op == Kind.OP_NEQ) {
			typeExprBinary = Type.BOOLEAN;
		} else if ((op == Kind.OP_GT || op == Kind.OP_LT || op == Kind.OP_GE || op == Kind.OP_LE)
				&& e0Type == Type.INTEGER) {
			typeExprBinary = Type.BOOLEAN;
		} else if ((op == Kind.OP_AND || op == Kind.OP_OR) && (e0Type == Type.INTEGER
				|| e0Type == Type.BOOLEAN)) {
			typeExprBinary = e0Type;
		} else if ((op == Kind.OP_DIV || op == Kind.OP_MINUS || op == Kind.OP_MOD || op == Kind.OP_PLUS
				|| op == Kind.OP_POWER || op == Kind.OP_TIMES) && (e0Type == Type.INTEGER)) {
			typeExprBinary = Type.INTEGER;
		} else {
			typeExprBinary = null;
		}
		if (e0Type == e1Type && typeExprBinary != null) {
			expression_Binary.typeVal =typeExprBinary; 
			return typeExprBinary;
		} else {

			throw new SemanticException(expression_Binary.firstToken, null);
		}
	}

	/*
	 * Expression_Unary ::= op Expression Expression_Unary.Type <= let t =
	 * Expression.Type in if op ∈ {EXCL} && (t == BOOLEAN || t == INTEGER) then t
	 * else if op {PLUS, MINUS} && t == INTEGER then INTEGER else Ʇ REQUIRE:
	 * Expression_ Unary.Type ≠ Ʇ
	 */
	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		Kind op = expression_Unary.op;
		Expression expr = expression_Unary.e;
		Type exprUnaryType;
		Type exprType = (Type) expr.visit(this, null);
		if (op == Kind.OP_EXCL && (exprType == Type.BOOLEAN || exprType == Type.INTEGER)) {
			exprUnaryType = exprType;
		} else if ((op == Kind.OP_PLUS || op == Kind.OP_MINUS) && (exprType == Type.INTEGER)) {
			exprUnaryType = Type.INTEGER;
		} else {
			exprUnaryType = null;
		}
		expression_Unary.typeVal = exprUnaryType;
		if(exprUnaryType!=null) {
		return exprUnaryType;}
		else {
			throw new SemanticException(expression_Unary.firstToken, "expressionunary type is null");
		}
	}
	/*
	 * Index ::= Expression0 Expression1 REQUIRE: Expression0.Type == INTEGER &&
	 * Expression1.Type == INTEGER Index.isCartesian <= !(Expression0 == KW_r &&
	 * Expression1 == KW_a)
	 */

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		Expression e0 = index.e0;
		Expression e1 = index.e1;
		if ((e0.visit(this, null) != Type.INTEGER) || (e1.visit(this, null) != Type.INTEGER )) 
		{
			throw new SemanticException(index.firstToken, "Index expression type not integer");
			}

			Token e0pKind = e0.firstToken;
			Token e1pKind = e1.firstToken;
			boolean isCartesian = true;
			if(e0pKind.kind == Kind.KW_r) {
			isCartesian = false;
					}			
			index.setCartesian(isCartesian);
			return isCartesian;
		} 
	

	/*
	 * Expression_PixelSelector ::= name Index name.Type <=
	 * SymbolTable.lookupType(name) Expression_PixelSelector.Type <= if name.Type ==
	 * IMAGE then INTEGER else if Index == null then name.Type else Ʇ REQUIRE:
	 * Expression_PixelSelector.Type ≠ Ʇ
	 */
	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		Type nameType = symbolTable.lookupType(expression_PixelSelector.name);
		Type exprPixlType;
		if (nameType == Type.IMAGE) {
			exprPixlType = Type.INTEGER;
		} else if (expression_PixelSelector.index == null) {
			exprPixlType = nameType;
		} else {
			exprPixlType = null;
		}
		expression_PixelSelector.typeVal = exprPixlType;
		if(exprPixlType!=null) {
		return exprPixlType;}
		else {
			throw new SemanticException(expression_PixelSelector.firstToken, 
					" pixel expression return type is null");
		}
	}

	/*
	 * Expression_Conditional ::= Expressioncondition Expressiontrue Expressionfalse
	 * REQUIRE: Expressioncondition.Type == BOOLEAN && Expressiontrue.Type
	 * ==Expressionfalse.Type Expression_Conditional.Type <= Expressiontrue.Type
	 */
	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		Expression exprCond = expression_Conditional.condition;
		Expression exprTrue = expression_Conditional.trueExpression;
		Expression exprFalse = expression_Conditional.falseExpression;
		exprCond.typeVal = (Type) exprCond.visit(this, null);
		exprTrue.typeVal = (Type) exprTrue.visit(this, null);
		exprFalse.typeVal = (Type) exprFalse.visit(this, null);
	

		if (exprCond.typeVal == Type.BOOLEAN && (exprTrue.typeVal == exprFalse.typeVal)) {
			expression_Conditional.typeVal = exprTrue.typeVal;
			return expression_Conditional.typeVal;
		}
		// TODO Auto-generated method stub
		else {
			throw new SemanticException(expression_Conditional.firstToken,null);
		}
	}

	/*
	 * REQUIRE: symbolTable.lookupType(name) = symbolTable.insert(name,
	 * Declaration_Image) Declaration_Image.Type <= IMAGE
	 */
	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) 
			throws Exception {
		String name = declaration_Image.name;
		Type t = symbolTable.lookupType(name); // it should not have been declared
		if (t != null) {
			throw new SemanticException(declaration_Image.firstToken, null);
		}
		symbolTable.insert(name, declaration_Image);
		declaration_Image.typeVal = Type.IMAGE;
		t = declaration_Image.typeVal;
		Expression exSize = declaration_Image.xSize;
		Expression eySize = declaration_Image.ySize;
		Type exVal = null;
		Type eyVal = null;
		if(exSize!=null && eySize!=null) {
		exVal = (Type) exSize.visit(this, null);
		eyVal = (Type) eySize.visit(this, null);
		if ((exVal==Type.INTEGER ) && (eyVal==Type.INTEGER)) {
			return t;
		}
		else {
			throw new SemanticException(declaration_Image.firstToken, null);
		}
		}		
return t;
}

	/*
	 * Source_StringLiteral ::= fileOrURL Source_StringLIteral.Type <= if
	 * isValidURL(fileOrURL) then URL else FILE
	 */
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		Type sourceStrType;
		if (source_StringLiteral.fileOrUrl.startsWith("http")) {
			sourceStrType = Type.URL;
		} else {
			sourceStrType = Type.FILE;
		}
		source_StringLiteral.typeVal = sourceStrType;
		return sourceStrType;

	}

	/*
	 * Source_CommandLineParam ::= ExpressionparamNum Source_CommandLineParam .Type
	 * <= ExpressionparamNum REQUIRE: Source_CommandLineParam .Type == INTEGER
	 */
	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {

		Expression exprParam = source_CommandLineParam.paramNum;
		source_CommandLineParam.typeVal = Type.NONE;
		Type exprType = (Type) exprParam.visit(this, null);
		if (exprType == Type.INTEGER) {
			return source_CommandLineParam.typeVal;
		} else {
			throw new SemanticException(source_CommandLineParam.firstToken,
					"source command line type is not integer");

		}
	}

	/*
	 * Source_Ident ::= name Sink_Ident.Type <= symbolTable.lookupType(name)
	 * REQUIRE: Sink_Ident.Type == FILE || Sink_Ident.Type == URL
	 */
	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {

		source_Ident.typeVal = symbolTable.lookupType(source_Ident.name);
		Type sourceType = source_Ident.typeVal;
		if (sourceType == Type.FILE || sourceType == Type.URL) {
			return sourceType;
		} else {
			throw new SemanticException(source_Ident.firstToken, 
					"source ident type is nether file nor url");
		}
	}

	/*
	 * REQUIRE: symbolTable.lookupType(name) = symbolTable.insert(name,
	 * Declaration_Image) Declaration_SourceSink.Type <= Type
	 */
	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		String name = declaration_SourceSink.name;
		Type t = symbolTable.lookupType(name); // it should not have been declared
		if (t != null) {
			throw new SemanticException(declaration_SourceSink.firstToken, null);
		}
		symbolTable.insert(name, declaration_SourceSink);
		declaration_SourceSink.typeVal = symbolTable.lookupType(name);
		t = declaration_SourceSink.typeVal;
		Source s = declaration_SourceSink.source;
		Type sourceType = (Type) s.visit(this, null);
		if (t == sourceType || sourceType==Type.NONE) {
			return t;
		}
		throw new SemanticException(declaration_SourceSink.firstToken, 
				"source sink declaration type not equal to source type");
	}

	/* Expression_IntLIt.Type <= INTEGER */
	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) 
			throws Exception {
		expression_IntLit.typeVal = Type.INTEGER;
		return Type.INTEGER;
	}

	/*
	 * Expression_FunctionAppWithExprArg ::= function Expression REQUIRE:
	 * Expression.Type == INTEGER Expression_FunctionAppWithExprArg.Type <= INTEGER
	 */
	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		Expression exprArg = expression_FunctionAppWithExprArg.arg;
		Type exprargType = (Type) exprArg.visit(this,null);
		exprArg.typeVal = exprargType;
		if (exprArg.typeVal == Type.INTEGER) {
			expression_FunctionAppWithExprArg.typeVal = Type.INTEGER;
			return Type.INTEGER;
		} else {
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, 
					"expr arg type not integer");
		}
		// TODO Auto-generated method stub

	}

	/*
	 * Expression_FunctionAppWithIndexArg ::= function Index
	 * Expression_FunctionAppWithExprArg.Type <= INTEGER
	 */
	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		expression_FunctionAppWithIndexArg.typeVal = Type.INTEGER;
		return Type.INTEGER;
	}

	/*
	 * Statement_In ::= name Source Statement_In.Declaration <= name.Declaration
	 * REQUIRE: (name.Declaration != null) & (name.type == Source.type)
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		Source s = statement_In.source;
		statement_In.source.typeVal = (Type)s.visit(this, null);
		Type sourceType = statement_In.source.typeVal;
		Type nameType = symbolTable.lookupType(statement_In.name);
		Declaration dec = symbolTable.getDec(statement_In.name);
		statement_In.setDec(dec);
        return nameType;
	}

	/*
	 * REQUIRE: LHS.Type == Expression.Type StatementAssign.isCartesian <=
	 * LHS.isCartesian
	 */

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		LHS l = statement_Assign.lhs;
		Type lhsType = (Type) l.visit(this, null);
		Expression expr = statement_Assign.e;
		Type exprType = (Type) expr.visit(this, null);
		
		if (lhsType == exprType) {
			statement_Assign.setCartesian(l.isCartesian);
			return lhsType;
		}
		else if(lhsType==Type.IMAGE && exprType==Type.INTEGER ) {
			statement_Assign.setCartesian(l.isCartesian);
			return lhsType;
		}
				else {
			// TODO Auto-generated method stub
			throw new SemanticException(statement_Assign.firstToken, null);
		}
	}

	/*
	 * LHS.Declaration <= symbolTable.lookupDec(name) LHS.Type <=
	 * LHS.Declaration.Type LHS.isCarteisan <= Index.isCartesian
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub

		lhs.setDeclaration( symbolTable.getDec(lhs.name));
		if (lhs.getDeclaration() == null) {
			throw new SemanticException(lhs.firstToken, null);
		}
		lhs.typeVal = symbolTable.lookupType(lhs.name);
		Index i = lhs.index;
		if(i!=null) {
		boolean indextype =  (boolean) i.visit(this, null);
		lhs.setCartesian(indextype);
		// throw new UnsupportedOperationException();
	}
		return lhs.typeVal;
	}
	/*
	 * Sink_SCREEN.Type <= SCREEN
	 */
	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		sink_SCREEN.typeVal = Type.SCREEN;
		return Type.SCREEN;
	}

	/*
	 * Sink_Ident ::= name Sink_Ident.Type <= symbolTable.lookupType(name) REQUIRE:
	 * Sink_Ident.Type == FILE
	 */
	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		sink_Ident.typeVal = symbolTable.lookupType(sink_Ident.name);
		if (sink_Ident.typeVal == Type.FILE) {
			return Type.FILE;
		} else {
			// TODO Auto-generated method stub
			throw new SemanticException(sink_Ident.firstToken,null);
		}
	}

	/*
	 * Expression_BooleanLit ::= value Expression_BooleanLit.Type <= BOOLEAN
	 */
	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		expression_BooleanLit.typeVal = Type.BOOLEAN;
		return Type.BOOLEAN;
	}

	/*
	 * Expression_Ident ::= name Expression_Ident.Type <=
	 * symbolTable.lookupType(name)
	 */
	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident, Object arg) throws Exception {
		expression_Ident.typeVal = symbolTable.lookupType(expression_Ident.name);
		return expression_Ident.typeVal;
	}

	/*
	 * Expression_PredefinedName ::= predefNameKind Expression_PredefinedName.TYPE
	 * <= INTEGER
	 */
	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {

		expression_PredefinedName.typeVal = Type.INTEGER;
		return Type.INTEGER;
	}

	/*
	 * Statement_Out ::= name Sink Statement_In.Declaration <= name.Declaration
	 * REQUIRE: (name.Declaration != null) REQUIRE: ((name.Type == INTEGER ||
	 * name.Type == BOOLEAN) && Sink.Type == SCREEN) || (name.Type == IMAGE &&
	 * (Sink.Type ==FILE || Sink.Type == SCREEN))
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Sink sink = statement_Out.sink;
		statement_Out.sink.typeVal = (Type)sink.visit(this, null);
		Type sinkType = statement_Out.sink.typeVal;
		Type nameType = symbolTable.lookupType(statement_Out.name);
		Declaration dec = symbolTable.getDec(statement_Out.name);
		statement_Out.setDec(dec);
		if ((dec != null) && ((nameType == Type.INTEGER || nameType == Type.BOOLEAN) && sinkType == Type.SCREEN)
				|| (nameType == Type.IMAGE && (sinkType == Type.FILE || sinkType == Type.SCREEN))) {
			return nameType;
		} else {
			throw new SemanticException(statement_Out.firstToken, null);
		}

	}

	public SymbolTable getSymbolTable() {
		// TODO Auto-generated method stub
		return symbolTable;
	}

}
