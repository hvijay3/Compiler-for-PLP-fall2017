package cop5556fa17;



import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashSet;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.*;
import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}

	Scanner scanner;
	Token firstToken;
	Token t;
	HashSet<Kind> declarationPredictSet =new HashSet<Kind>(Arrays.asList(KW_int, KW_boolean, KW_url, KW_file, KW_image));
	HashSet<Kind> statementPredictSet= new HashSet<Kind>(Arrays.asList(IDENTIFIER));
	HashSet<Kind> primaryPredictSet= new HashSet<Kind>(Arrays.asList(INTEGER_LITERAL, LPAREN, BOOLEAN_LITERAL));
	HashSet<Kind> unarySet = new HashSet<Kind>(Arrays.asList(KW_x, KW_y , KW_r , KW_a , KW_X , KW_Y , KW_Z 
			, KW_A , KW_R , KW_DEF_X , KW_DEF_Y));
	HashSet<Kind> functionNameSet =new HashSet<Kind>(Arrays.asList( KW_sin, KW_cos , KW_atan , KW_abs 
			, KW_cart_x , KW_cart_y , KW_polar_a , KW_polar_r )); ;
	
	

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	/**
	 * Program ::= IDENTIFIER ( Declaration SEMI | Statement SEMI )*
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	public Program program() throws SyntaxException {
		firstToken=t;
		ArrayList<ASTNode> decAndStatements = new ArrayList<ASTNode>();
		Token firstT = t;	
		match(Kind.IDENTIFIER);
		while (isInDeclarationPredictSet(t) || isInStatemenetPredictSet(t)) {
			if (isInDeclarationPredictSet(t)) {
				Declaration declaration = declaration();
				decAndStatements.add(declaration);
			} else if (isInStatemenetPredictSet(t)) {
				Statement statement = statement();
				decAndStatements.add(statement);
			}
			match(Kind.SEMI);
		}
		return new Program(firstToken, firstT, decAndStatements);
		// TODO implement this
		// throw new UnsupportedOperationException();
	}
	
	
	private Declaration declaration() throws SyntaxException {

		if (t.kind == Kind.KW_int || t.kind == Kind.KW_boolean) {
			Declaration_Variable decVar = variableDeclaration();
			return decVar;
		} else if (t.kind == Kind.KW_image) {
			Declaration_Image decImage = imageDeclaration();
			return decImage;
		} else if (t.kind == Kind.KW_url || t.kind == Kind.KW_file) {
			Declaration_SourceSink decSourceSink = sourceSinkDeclaration();
			return decSourceSink;
		} else {
			String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}


	}

	
	/*
	 * Statement ::= AssignmentStatement | ImageOutStatement | ImageInStatement
	 * ImageInStatement ::= IDENTIFIER OP_LARROW Source 
	 * AssignmentStatement ::= Lhs OP_ASSIGN Expression
	 * Lhs::=  IDENTIFIER ( LSQUARE LhsSelector RSQUARE   | e )
	 * ImageOutStatement ::= IDENTIFIER OP_RARROW Sink 
	 * Sink ::= IDENTIFIER | KW_SCREEN //ident must be file
	 * 
	 */

	private Statement statement() throws SyntaxException {
		firstToken = t;
		Token name = t;
		match(IDENTIFIER);
		if (t.kind == Kind.OP_RARROW) {
			Statement_Out statementOut = imageOutStatement(firstToken , name);
			return statementOut;
		} else if (t.kind == Kind.OP_LARROW) {
			Statement_In statementIn = imageInStatement(firstToken, name);
			return statementIn;
		} else {
			Statement_Assign statementAssign =  assignmentStatement(firstToken, name);
			return statementAssign;
		}

	}

	// AssignmentStatement ::= Lhs OP_ASSIGN Expression
	//public Statement_Assign(Token firstToken, LHS lhs, Expression e)
	private  Statement_Assign assignmentStatement(Token firstToken , Token name) throws SyntaxException {
		LHS lhs = lhs(firstToken , name);
		match(Kind.OP_ASSIGN);
		Expression expression = expression();
		return new Statement_Assign(firstToken, lhs, expression);
	}

	//Lhs::= IDENTIFIER (LSQUARE LhsSelector RSQUARE | e )
	//public LHS(Token firstToken, Token name, Index index)
	private LHS lhs(Token firstToken, Token name) throws SyntaxException {
		Index index = null;
		if (t.kind == Kind.LSQUARE) {
			match(Kind.LSQUARE);
			index = lhsSelector();
			match(Kind.RSQUARE);
		}
		return new LHS(firstToken, name, index);
	}

	
//public Index(Token firstToken, Expression e0, Expression e1)
	private Index lhsSelector() throws SyntaxException {
		Token firstToken = t;
		Expression_PredefinedName e0;
		Expression_PredefinedName e1;
		match(Kind.LSQUARE);
		if(t.kind==KW_x)
		{
			Token t1 = t;
			match(KW_x);
			e0 = new Expression_PredefinedName(firstToken,t1.kind);
			match(COMMA);
			Token t2 = t;
			match(KW_y);
			e1 = new Expression_PredefinedName(firstToken,t2.kind);
			match(Kind.RSQUARE);
			return new Index(firstToken ,e0,e1 );
		}
		else if(t.kind==KW_r)
		{
			Token t1 = t;
			match(KW_r);
			e0 = new Expression_PredefinedName(firstToken,t1.kind);
			match(COMMA);
			Token t2 = t;
			match(KW_a);
			e1 = new Expression_PredefinedName(firstToken,t2.kind);
			match(Kind.RSQUARE);
			return new Index(firstToken ,e0,e1 );
		}
		else
		{
			String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		// TODO Auto-generated method stub

	}

	//ImageInStatement ::= IDENTIFIER OP_LARROW Source
	//public Statement_In(Token firstToken, Token name, Source source)
	private Statement_In imageInStatement(Token firstToken , Token name) throws SyntaxException {
		match(Kind.OP_LARROW);
		Source source = source();
		return new Statement_In(firstToken, name, source);
		// TODO Auto-generated method stub

	}

	// ImageOutStatement ::= IDENTIFIER OP_RARROW Sink
	// Sink ::= IDENTIFIER | KW_SCREEN //ident must be file
	//public Statement_Out(Token firstToken, Token name, Sink sink)
	private Statement_Out imageOutStatement(Token firstToken, Token name1) throws SyntaxException {
		Token name = name1;
		match(Kind.OP_RARROW);
		Sink sink = sink();
		return new Statement_Out(firstToken, name,sink );

	}

	// Sink ::= IDENTIFIER | KW_SCREEN //ident must be file
	//Sink ::= IDENTIFIER 	Sink_Ident
	//Sink ::= KW_SCREEN  	Sink_SCREEN

	private Sink sink() throws SyntaxException {
		Token firstToken = t;
		if (t.kind == IDENTIFIER || t.kind == KW_SCREEN) {
			if (t.kind == IDENTIFIER) {
				Token name = t;
				match(t.kind);
				return new Sink_Ident(firstToken, name);
			} else {
				match(t.kind);
				return new Sink_SCREEN(firstToken);
			}

		} else {
			String message = "Expected at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}

	}



	// ImageDeclaration ::= KW_image (LSQUARE Expression COMMA Expression RSQUARE |
	// e)
	// IDENTIFIER ( OP_LARROW Source | e )

	//	public Declaration_Image(Token firstToken, Expression xSize, Expression ySize, Token name,
	//Source source)
	private Declaration_Image imageDeclaration() throws SyntaxException {
		Token firstToken = t;
		Expression xSize = null;
		Expression ySize = null;
		Token name =null;
		Source source = null;
		match(KW_image);
		if (t.kind == Kind.LSQUARE) {
			match(Kind.LSQUARE);
			 xSize = expression();
			match(COMMA);
			ySize = expression();
			match(Kind.RSQUARE);

		}
		name = t;
		match(IDENTIFIER);
		if (t.kind == OP_LARROW) {
			match(OP_LARROW);
			source = source();
		}
		return new Declaration_Image(firstToken,xSize,ySize,name,source);

	}
	//Source ::= STRING_LITERAL  
	//Source ::= OP_AT Expression 
	//Source ::= IDENTIFIER  
	//Source_StringLiteral
	//Source_CommandLIneParam
	//Source_Ident
	//public Source_CommandLineParam(Token firstToken, Expression paramNum)
	//public Source_StringLiteral(Token firstToken, String fileOrUrl)
	//public Source_Ident(Token firstToken, Token name)

	private Source source() throws SyntaxException 
	{
		Token firstToken = t;
		Expression paramNum = null;
		
		if (t.kind == Kind.STRING_LITERAL || t.kind == Kind.IDENTIFIER) {
			if(t.kind==Kind.STRING_LITERAL) {
				String fileorUrl = t.getText();
				match(t.kind);
				return new Source_StringLiteral(firstToken, fileorUrl);
			}
			else {
				Token name = t;
				match(t.kind);
			return new Source_Ident(firstToken, name);
			}
		} else if (t.kind == Kind.OP_AT) {
			match(OP_AT);
			paramNum = expression();
			return new Source_CommandLineParam(firstToken, paramNum);
		} else {
			String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		// TODO Auto-generated method stub

	}

	// VarType IDENTIFIER ( OP_ASSIGN Expression | e )
	private Declaration_Variable variableDeclaration() throws SyntaxException {
		Token firstToken = t;
		Token vartype = t;
		Expression expression = null;
		match(t.kind);
		Token name = t;
		match(IDENTIFIER);

		if (t.kind == Kind.OP_ASSIGN) {
			match(OP_ASSIGN);
			expression = expression();
		}
		return new Declaration_Variable(firstToken, vartype, name, expression);

		// TODO Auto-generated method stub

	}

	private boolean isInStatemenetPredictSet(Token t2) {
		if (statementPredictSet.contains(t2.kind)) {
			return true;
		}
		else {
		// TODO Auto-generated method stub
		return false;
	}
		}

	private boolean isInDeclarationPredictSet(Token t2) {
		// TODO Auto-generated method stub
		if (declarationPredictSet.contains(t2.kind)) {
			return true;
		}
		else {
			return false;
		}
		
	}
	// SourceSinkType IDENTIFIER OP_ASSIGN Source
	// public Declaration_SourceSink(Token firstToken, Token type, Token name, Source source)

	private Declaration_SourceSink sourceSinkDeclaration() throws SyntaxException {
		Token firstToken = t;
		Source source=null;
		Token type = t;
			match(t.kind);
			Token name =t;
			match(IDENTIFIER);
			match(OP_ASSIGN);
			source = source();
			return new Declaration_SourceSink(firstToken,type,name,source);
		
	}

	/**
	 * Expression ::= OrExpression OP_Q Expression OP_COLON Expression |
	 * OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental
	 * development.
	 * 
	 * @throws SyntaxException
	 */
	//public Expression_Conditional(Token firstToken, Expression condition, Expression trueExpression,
	//Expression falseExpression) {
	//Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression
    //|   OrExpression

	public Expression expression() throws SyntaxException {
		// TODO implement this.
		//Expression nullCondition = null;
		//Expression Expression_Conditional = null;
		Token firstToken = t;
		Expression condition = orExpression();
		if(t.kind==OP_Q)
		{
			match(OP_Q);
			Expression trueExpression = expression();
			match(OP_COLON);
			Expression falseExpression = expression();
			return new Expression_Conditional(firstToken, condition ,trueExpression,falseExpression);
		}
		return condition;
		//throw new UnsupportedOperationException();
	}


//OrExpression ::= AndExpression   (  OP_OR  AndExpression)*
	//public Expression_Binary(Token firstToken, Expression e0, Token op, Expression e1
	private Expression orExpression() throws SyntaxException {
		Token firstToken = t;
		        Expression e0 = andExpression();
				Expression e1 = null;
				Token op = null;
				//Expression_Binary expressionBinary = new Expression_Binary(firstToken,e0,op,e1);
				Expression expressionBinary = e0;

				while(t.kind==OP_OR)
				{ 
					op = t;
					match(OP_OR);
					e1 = andExpression();
					expressionBinary = new Expression_Binary(firstToken,expressionBinary,op,e1);

				}
			return expressionBinary;
		
		
	}
//AndExpression ::= EqExpression ( OP_AND  EqExpression )*

	private Expression andExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = eqExpression();
		Expression e1 = null;
		Token op = null;
		//Expression_Binary expressionBinary = new Expression_Binary(firstToken,e0,op,e1);
		Expression expressionBinary = e0;
		while(t.kind==OP_AND)
		{
			op = t;
			match(OP_AND);
			e1 = eqExpression();
			expressionBinary = new Expression_Binary(firstToken,expressionBinary,op,e1);
		}
		return expressionBinary;
		
	}

//EqExpression ::= RelExpression  (  (OP_EQ | OP_NEQ )  RelExpression )*

	private Expression eqExpression() throws SyntaxException {
		Token firstToken = t;
		// TODO Auto-generated method stub
		Expression e0 = relExpression();
		Expression e1 = null;
		Token op = null;
		//Expression_Binary expressionBinary = new Expression_Binary(firstToken,e0,op,e1);
		Expression expressionBinary = e0;
		
		while(t.kind==OP_EQ || t.kind==OP_NEQ)
		{
			op = t;
			match(t.kind);
			e1 = relExpression();
			expressionBinary = new Expression_Binary(firstToken,expressionBinary,op,e1);
		}
		return expressionBinary;
	}
//RelExpression ::= AddExpression (  ( OP_LT  | OP_GT |  OP_LE  | OP_GE )   AddExpression)*
	private Expression relExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = addExpression();
		Expression e1 = null;
		Token op = null;
		//Expression_Binary expressionBinary = new Expression_Binary(firstToken,e0,op,e1);
		Expression expressionBinary = e0;
		
		while(t.kind==OP_LT || t.kind==OP_GT || t.kind==OP_LE || t.kind==OP_GE)
		{
			op = t;
			match(t.kind);
			e1 = addExpression();
			expressionBinary = new Expression_Binary(firstToken,expressionBinary,op,e1);
		}
		return expressionBinary;
	}

	//AddExpression ::= MultExpression   (  (OP_PLUS | OP_MINUS ) MultExpression )*

	private Expression addExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = multExpression();
		Expression e1 = null;
		Token op = null;
		//Expression_Binary expressionBinary = new Expression_Binary(firstToken,e0,op,e1);
		Expression expressionBinary = e0;
		
		while(t.kind==OP_PLUS || t.kind==OP_MINUS)
		{
			op = t;
			match(t.kind);
			e1 = multExpression();
			expressionBinary = new Expression_Binary(firstToken,expressionBinary,op,e1);
		}
		return expressionBinary;
	}
	
	
//UnaryExpression ( ( OP_TIMES | OP_DIV  | OP_MOD ) UnaryExpression )*
	private Expression multExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = unaryExpression();
		Expression e1 = null;
		Token op = null;
		//Expression_Binary expressionBinary = new Expression_Binary(firstToken,e0,op,e1);
		Expression expressionBinary = e0;
		while(t.kind==OP_TIMES||t.kind==OP_DIV || t.kind == OP_MOD)
		{
			op = t;
			match(t.kind);
			e1 = unaryExpression();
			expressionBinary = new Expression_Binary(firstToken,expressionBinary,op,e1);
		}
		return expressionBinary;
		// TODO Auto-generated method stub
		
	}



//UnaryExpression ::= OP_PLUS UnaryExpression 
   // | OP_MINUS UnaryExpression 
   // | UnaryExpressionNotPlusMinus
	//public Expression_Unary(Token firstToken, Token op, Expression e)

	private Expression unaryExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e = null;
		Token op = null;
		if(t.kind==OP_PLUS)
		{
			op =t;
		match(OP_PLUS);	
		e = unaryExpression();
		return new Expression_Unary(firstToken,op,e);
		
		}
		else if (t.kind==OP_MINUS)
		{
			op =t;
			match(OP_MINUS);
			e = unaryExpression();
			return new Expression_Unary(firstToken,op,e);
		}
		else
		{
			e = unaryExpressionNotPlusMinus();
			return e;
		}
		// TODO Auto-generated method stub
		
	}
//UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary 
	//| IdentOrPixelSelectorExpression | KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y

	private Expression unaryExpressionNotPlusMinus() throws SyntaxException {
		Token firstToken = t;
		Expression e = null;
		Token op = null;
		if(t.kind==OP_EXCL)
		{
			op = t;
			match(OP_EXCL);
			e = unaryExpression();
			return new Expression_Unary(firstToken,op,e);
		}
		else if (unarySet.contains(t.kind))
		{
			
			op = t;
			match(t.kind);
			return new Expression_PredefinedName(firstToken,op.kind);
		}
		else if (t.kind==IDENTIFIER)
		{
			e = identOrPixelSelectorExpression();
			return e;
		}
		
		//primarypredictset
		else if(isInPrimaryPredictSet(t.kind))
		{
			e = primary();
			return e;
		}
		else
		{
			String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		// TODO Auto-generated method stub
		
		
	}
	



private boolean isInPrimaryPredictSet(Kind kind) {
	if(primaryPredictSet.contains(kind) || functionNameSet.contains(kind)) {
		return true;
	}
	else {
	return false;
}}

//Primary ::= INTEGER_LITERAL | LPAREN Expression RPAREN | FunctionApplication | Boolean literal
	private Expression primary() throws SyntaxException {
		Token firstToken = t;
		Expression e = null;
		if(t.kind== INTEGER_LITERAL)
		{
			Token intLit = t;
			match(INTEGER_LITERAL);
			return new Expression_IntLit(firstToken , intLit.intVal());
		}
		else if(t.kind==Kind.LPAREN)
		{
			match(Kind.LPAREN);
			e = expression();
			match(Kind.RPAREN);
			return e;
		}
		else if(functionNameSet.contains(t.kind))
		{
			e = functionApplication();
			return e;
		}
		else if(t.kind==Kind.BOOLEAN_LITERAL)
		{
			Token booleanLit = t;
			match(Kind.BOOLEAN_LITERAL);
			boolean value = booleanLit.getText().equals("true")?true:false;
			return new Expression_BooleanLit(firstToken , value);
		}
		else
		{
			String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		
		// TODO Auto-generated method stub
		
	}

//FunctionApplication ::= FunctionName LPAREN Expression RPAREN  	Expression_FunctionAppWithExprArg
	//ublic Expression_FunctionAppWithExprArg(Token firstToken, Kind function, Expression arg)
	private Expression functionApplication() throws SyntaxException {
		Token firstToken = t;
		Token op = t;
		match(t.kind);
		if(t.kind == Kind.LPAREN)
		{
		match(Kind.LPAREN);
		Expression arg = expression();
		match(Kind.RPAREN);
		return new Expression_FunctionAppWithExprArg(firstToken, op.kind, arg);
		}
		else if(t.kind == Kind.LSQUARE)
		{
			match(Kind.LSQUARE);
			Index arg = selector();
			match(Kind.RSQUARE);	
		    return new Expression_FunctionAppWithIndexArg(firstToken, op.kind, arg);
		}
		else
		{
			String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		
	}
//Selector ::=  Expression COMMA Expression
	private Index selector() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = expression();
		match(COMMA);
		Expression e1 = expression();
		return new Index(firstToken,e0,e1);
		// TODO Auto-generated method stub
		
	}

	
	//IdentOrPixelSelectorExpression::=  IDENTIFIER LSQUARE Selector RSQUARE   | IDENTIFIER
	//IdentOrPixelSelectorExpression::=  IDENTIFIER LSQUARE Selector RSQUARE   	Expression_PixelSelector
	//IdentOrPixelSelectorExpression::=  IDENTIFIER	Expression_Ident
//public Expression_PixelSelector(Token firstToken, Token name, Index index)
	//public Expression_Ident(Token firstToken, Token ident)
	private Expression identOrPixelSelectorExpression() throws SyntaxException {
		Token firstToken = t;
		Token name = t;
		Index index = null;
		match(IDENTIFIER);
		if(t.kind==Kind.LSQUARE)
		{
			match(LSQUARE);
			index = selector();
			match(Kind.RSQUARE);
			return new Expression_PixelSelector(firstToken, name, index);
		}
		return new Expression_Ident(firstToken,name);
		// TODO Auto-generated method stub
		
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}

	private void match(Kind kind) throws SyntaxException {
		if (t.kind == kind) {
			consumeToken();
			return;
		} else {
			String message = "Exception at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
	}

	private void consumeToken() {
		if (scanner.hasTokens()) {
			t = scanner.nextToken();
		}
		// TODO Auto-generated method stub

	}
}
