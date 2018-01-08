package cop5556fa17;

import java.util.ArrayList;


import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv;

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	//SymbolTable symbolTable;


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		// if GRADE, generates code to add string to log

		//CodeGenUtils.genLog(GRADE, mv, "entering main");
		//  visit decs and statements to add field to class
		//  and instructions to main method, respectively

		mv.visitLdcInsn(256);
		mv.visitVarInsn(ISTORE, 8);
		mv.visitLdcInsn(256);
		mv.visitVarInsn(ISTORE, 9);
		mv.visitLdcInsn(16777215);
		mv.visitVarInsn(ISTORE, 7);
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("x", "I", null, mainStart, mainEnd, 1);
		mv.visitLocalVariable("y", "I", null, mainStart, mainEnd, 2);
		mv.visitLocalVariable("r", "I", null, mainStart, mainEnd, 3);
		mv.visitLocalVariable("a", "I", null, mainStart, mainEnd, 4);
		mv.visitLocalVariable("X", "I", null, mainStart, mainEnd, 5);
		mv.visitLocalVariable("Y", "I", null, mainStart, mainEnd, 6);
		mv.visitLocalVariable("Z", "I", null, mainStart, mainEnd, 7);
		mv.visitLocalVariable("DEF_X", "I", null, mainStart, mainEnd, 8);
		mv.visitLocalVariable("DEF_Y", "I", null, mainStart, mainEnd, 9);
		mv.visitLocalVariable("R", "I", null, mainStart, mainEnd, 10);
		mv.visitLocalVariable("A", "I", null, mainStart, mainEnd, 11);
		
		//Setting local variable here x-1, y-2, X-5, Y-6, r-3, a-4, R-10, A-11, and Z-7



		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}
/*Add field, name, as static member of class.  
	If there is an expression, generate code to evaluate it and store the results in the field.
	See comment about this below.
*/
	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) 
			throws Exception {	
		String field = declaration_Variable.name;
		String fieldType = declaration_Variable.typeVal==Type.BOOLEAN?"Z":"I";	
		fv = cw.visitField(ACC_STATIC, field, fieldType, null, null);
		fv.visitEnd();			
		Expression expr = declaration_Variable.e;
		if(expr!=null) {
		expr.visit(this, null);		
		mv.visitFieldInsn(PUTSTATIC, className, field, fieldType );	}	
		return null;
	}

	/*
	 * Generate code to evaluate the expression and 
	 * leave the value on top of the stack.
Visiting the nodes for Expression0 and Expression1  \
will generate code to leave those values on the stack.  
Then just generate code to perform the op.*/
	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) 
			throws Exception {
		// TODO
		Expression e0 = expression_Binary.e0;
		Expression e1 = expression_Binary.e1;
		Type e0Type = e0.typeVal;
		Type e1Type = e1.typeVal;
		Label trueLabel = new Label();
		Label falseLabel = new Label();
		if (e0 != null) {
			e0.visit(this, null);
		}
		if (e1 != null) {
			e1.visit(this, null);
		}

		if (e1 != null) {
			Kind opCode = expression_Binary.op;
/*			if ((e0Type == Type.INTEGER && e1Type == Type.INTEGER)
					|| (e0Type == Type.BOOLEAN && e1Type == Type.BOOLEAN)) {*/

				if (opCode == Kind.OP_PLUS) {
					mv.visitInsn(IADD);
					mv.visitJumpInsn(GOTO, falseLabel);
				} else if (opCode == Kind.OP_MINUS) {
					mv.visitInsn(ISUB);
					mv.visitJumpInsn(GOTO, falseLabel);
				} else if (opCode == Kind.OP_LT) {
					mv.visitJumpInsn(IF_ICMPLT, trueLabel);
					mv.visitLdcInsn(false);
					mv.visitJumpInsn(GOTO, falseLabel);

				} else if (opCode == Kind.OP_GT) {
					mv.visitJumpInsn(IF_ICMPGT, trueLabel);
					mv.visitLdcInsn(false);
					mv.visitJumpInsn(GOTO, falseLabel);
				} else if (opCode == Kind.OP_LE) {
					mv.visitJumpInsn(IF_ICMPLE, trueLabel);
					mv.visitLdcInsn(false);
					mv.visitJumpInsn(GOTO, falseLabel);
				} else if (opCode == Kind.OP_GE) {
					mv.visitJumpInsn(IF_ICMPGE, trueLabel);
					mv.visitLdcInsn(false);
					mv.visitJumpInsn(GOTO, falseLabel);
				} else if (opCode == Kind.OP_EQ) {
					mv.visitJumpInsn(IF_ICMPEQ, trueLabel);
					mv.visitLdcInsn(false);
					mv.visitJumpInsn(GOTO, falseLabel);
				} else if (opCode == Kind.OP_NEQ) {
					mv.visitJumpInsn(IF_ICMPNE, trueLabel);
					mv.visitLdcInsn(false);
					mv.visitJumpInsn(GOTO, falseLabel);
				} else if (opCode == Kind.OP_TIMES) {
					mv.visitInsn(IMUL);
					mv.visitJumpInsn(GOTO, falseLabel);
				} else if (opCode == Kind.OP_MOD) {
					mv.visitInsn(IREM);
					mv.visitJumpInsn(GOTO, falseLabel);
				} else if (opCode == Kind.OP_DIV) {
					mv.visitInsn(IDIV);
					mv.visitJumpInsn(GOTO, falseLabel);

				} else if (opCode == Kind.OP_OR) {
					mv.visitInsn(IOR);
					mv.visitJumpInsn(GOTO, falseLabel);
				} else if (opCode == Kind.OP_AND) {
					mv.visitInsn(IAND);
					mv.visitJumpInsn(GOTO, falseLabel);
				}
				mv.visitLabel(trueLabel);
				
				if (opCode != Kind.OP_PLUS && opCode != Kind.OP_MINUS && opCode != Kind.OP_DIV 
						&& opCode != Kind.OP_TIMES && opCode != Kind.OP_MOD && opCode != Kind.OP_OR && opCode!=Kind.OP_AND) {
				mv.visitLdcInsn(true);
			}

		}
		mv.visitLabel(falseLabel);
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.typeVal);
		return null;
	}
/*
 * Generate code to evaluate the unary expression and leave its value on top of the stack.
               Which code is generated will depend on the operator.  If the op is OP_PLUS, the value 
that should be left on the stack is just the value of Expression 
*/
	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		Expression e  =expression_Unary.e;
		Type t = expression_Unary.typeVal;
		Kind kop = expression_Unary.op;
		e.visit(this, null);
		if ( kop == Kind.OP_MINUS) {
			mv.visitLdcInsn(-1);
			mv.visitInsn(IMUL);
		}
		else if(kop == Kind.OP_EXCL) {
			if(t==Type.BOOLEAN) {
			mv.visitLdcInsn(true);
			Label trueLabel = new Label();
			mv.visitJumpInsn(IF_ICMPEQ, trueLabel);
			mv.visitLdcInsn(true);
			Label falseLabel = new Label();
			mv.visitJumpInsn(GOTO, falseLabel);
			mv.visitLabel(trueLabel);
			mv.visitLdcInsn(false);	
			mv.visitLabel(falseLabel);
		}
			else if(t==Type.INTEGER) {
				mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));
				mv.visitInsn(IXOR);
			}
		}	
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.typeVal);
		return null;
	}

	/*Visit the expressions to leave the values on top of the stack.  If isCartesian, you are done. 
	 *  If not, generate code to convert r and a to x and y using (cart_x and cart_y).  
	 *  Hint:  you will need to manipulate the stack a little bit to handle the two values. 
	 *   You may find  DUP2, DUP_X2, and POP useful.  */
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		Expression x = index.e0;
		Expression y = index.e1;
		x.visit(this, null);
		y.visit(this, null);
		Kind kind = x.firstToken.kind;
		
		if(kind == Kind.KW_r)
		{
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
            mv.visitInsn(DUP_X2);
            mv.visitInsn(POP);
            mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);

		}
		
		return null;
	}
/*Generate code to load the image reference on the stack.  
 * Visit the index to generate code to leave Cartesian location of index on the stack. 
 *  Then  invoke ImageSupport.getPixel which generates code to leave the value of the pixel on the stack.*/
	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		String field = expression_PixelSelector.name;
		mv.visitFieldInsn(GETSTATIC, className, field , ImageSupport.ImageDesc);
		Index index = expression_PixelSelector.index;
		index.visit(this, null);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", 
				ImageSupport.getPixelSig, false);
		
	return null;
	}

	/*
	 * Generate code to evaluate the Expressioncondition and depending on its
              Value, to leave the value of either Expressiontrue  or 
              Expressionfalse on top of the stack.
              Hint:  you will need to use labels,  a conditional instruction, and goto.
*/
	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		Expression condition = expression_Conditional.condition;
		Expression trueCondition = expression_Conditional.trueExpression;
		Expression falseCondition = expression_Conditional.falseExpression;
		
		condition.visit(this, null);
		mv.visitLdcInsn(true);
		Label falseExpr = new Label();
		mv.visitJumpInsn(IF_ICMPNE, falseExpr);
		trueCondition.visit(this,null);
		Label trueExpr = new Label();
		mv.visitJumpInsn(GOTO, trueExpr);
		
		mv.visitLabel(falseExpr);
		falseCondition.visit(this,null);
		mv.visitLabel(trueExpr);
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.typeVal);
		return null;
	}

/*ImageDeclaration ::= KW_image (LSQUARE Expression COMMA
Expression RSQUARE | ) IDENTIFIER ( OP_LARROW Source | ε )*/
	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		String field = declaration_Image.name;
		String fieldType = "Ljava/awt/image/BufferedImage;";
		fv = cw.visitField(ACC_STATIC, field, fieldType, null, null);
		fv.visitEnd();	
		Source source = declaration_Image.source;
		Expression exprX = declaration_Image.xSize;
		Expression exprY = declaration_Image.ySize;
		if(source!=null) {
			source.visit(this, null);
		
		if(exprX!=null && exprY!=null) {
			exprX.visit(this, null);
			//mv.visitInsn(DUP);
			//mv.visitVarInsn(ISTORE,5);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", 
					"(I)Ljava/lang/Integer;", false);
			exprY.visit(this, null);
			//mv.visitInsn(DUP);
			//mv.visitVarInsn(ISTORE,6);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", 
					"(I)Ljava/lang/Integer;", false);
		}
		else
		{
			mv.visitLdcInsn(new Integer(null));
			mv.visitLdcInsn(new Integer(null));
		}
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", 
				ImageSupport.readImageSig, false);	
		}
		else {
			if(exprX!=null && exprY!=null) {
				exprX.visit(this, null);
				exprY.visit(this, null);
			}
			else {
			mv.visitVarInsn(ILOAD, 8);
				mv.visitVarInsn(ILOAD, 9);
				
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", 
					ImageSupport.makeImageSig, false);
		}
		mv.visitFieldInsn(PUTSTATIC, className, field, fieldType );	
		return null;
	}
	
  /*
   * Load the String onto the stack*/
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
        return null;
	}

	
/*
 * Generate code to evaluate the expression and use aaload to read the element from the
 *  command line array using the expression value as the index.  
 * The command line array is the String[] args param passed to main.*/
	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		
		// put array on stack
		mv.visitVarInsn(ALOAD, 0);
		Expression expr = source_CommandLineParam.paramNum;
		expr.visit(this, null);
		mv.visitInsn(AALOAD);
		return null;
	}
/*
 * This identifier refers to a String.  Load it onto the stack.*/
	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name ,"Ljava/lang/String;" );
		//mv.visitLdcInsn(source_Ident.name);
		return null;
	}

/*Add a field to the class with the given name.  
 * If there is a Source, visit it to generate code to leave a String 
 * describing the sport on top of the stack and then write it to the field.  */
	
	/*SourceSinkDeclaration ::= SourceSinkType IDENTIFIER OP_ASSIGN Source*/
	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		String field = declaration_SourceSink.name;
		String fieldType = "Ljava/lang/String;";
		Source source = declaration_SourceSink.source;
		fv = cw.visitField(ACC_STATIC, field, fieldType, null, null);
		fv.visitEnd();
		if(source!=null) {
		source.visit(this, arg);
		mv.visitFieldInsn(PUTSTATIC, className, field, fieldType );	}	
		return null;

	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		mv.visitLdcInsn(expression_IntLit.value);
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	/*Visit the expression to generate code to leave its value on top of the stack.  
	 * Then invoke the corresponding function in RuntimeFunctions.  
	 * The functions that belong here are abs and log.
(You do not need to implement sin, cos, or atan)
*/
	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		
		Expression exprArg = expression_FunctionAppWithExprArg.arg;
		exprArg.visit(this, null);
		
		Kind func = expression_FunctionAppWithExprArg.function;
		if(func==Kind.KW_abs) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
		}
		else if(func == Kind.KW_log) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
		}
        return null;
	}

	/* Visit the index to leave two values on top of the stack.  
	 * Then invoke the corresponding function in RuntimeFunctions.  
	 * The functions that belong here are cart_x, cart_y, polar_r, and polar_a. 
	 *  These functions convert between the cartesian (x,y) and polar (r,a) 
	 * (i.e. radius and angle in degrees) representations of the location in the image.*/
	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		Index index = expression_FunctionAppWithIndexArg.arg;
		Expression e0 = index.e0;
		Expression e1 = index.e1;
		e0.visit(this, null);
		e1.visit(this,null);
		//index.visit(this, null);
		Kind func = expression_FunctionAppWithIndexArg.function;
		if(func==Kind.KW_cart_x) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
		}
		else if(func == Kind.KW_cart_y) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}
		else if(func == Kind.KW_polar_r) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		}
		else if(func == Kind.KW_polar_a)  {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		}
		return null;
	}
/*
 * Generate code to load value of variable onto the stack.  See comments below.*/
	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		Kind kdef = expression_PredefinedName.kind;
         if (kdef == Kind.KW_x) {
			mv.visitVarInsn(ILOAD, 1);
		} else if (kdef == Kind.KW_y) {
			mv.visitVarInsn(ILOAD, 2);
		} else if (kdef == Kind.KW_r) {
			mv.visitVarInsn(ILOAD, 3);
		} else if (kdef == Kind.KW_a) {
			mv.visitVarInsn(ILOAD, 4);
		} 
		else if (kdef == Kind.KW_X) {
			mv.visitVarInsn(ILOAD, 5);
		} else if (kdef == Kind.KW_Y) {
			mv.visitVarInsn(ILOAD, 6);
		}
		else if (kdef == Kind.KW_Z) {
			mv.visitVarInsn(ILOAD, 7);
		} else if (kdef == Kind.KW_DEF_X) {
			mv.visitVarInsn(ILOAD, 8);
		} else if (kdef == Kind.KW_DEF_Y) {
			mv.visitVarInsn(ILOAD, 9);
		} else if (kdef == Kind.KW_R) {
			mv.visitVarInsn(ILOAD, 10);
		} 
		else if (kdef == Kind.KW_A) {
			mv.visitVarInsn(ILOAD, 11);
		}

		return null;
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5: only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		Sink sink = statement_Out.sink;
		String field = statement_Out.name;
		Type fieldType = statement_Out.getDec().typeVal;
		String fieldTypeChar= "";
		if(fieldType==Type.INTEGER) {
			fieldTypeChar = "I";	
		}
	 if(fieldType == Type.BOOLEAN) {
			fieldTypeChar = "Z";
		}
		 if(fieldType == Type.IMAGE) {
			fieldTypeChar = "Ljava/awt/image/BufferedImage;";	
		}
		if(fieldType == Type.INTEGER || fieldType== Type.BOOLEAN) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		}
				mv.visitFieldInsn(GETSTATIC, className, field, fieldTypeChar);	

		CodeGenUtils.genLogTOS(GRADE, mv, fieldType);
		 if (fieldType == Type.IMAGE) {
			sink.visit(this, null);
		}
		 if(fieldType!=Type.IMAGE) {
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "("+fieldTypeChar+")"+"V", false);
		 }
		return null;

	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	/*Generate code to get value from the source and store it in variable name.
	For Assignment 5, the only source that needs to be handled is the command line.

Visit source to leave string representation of the value on top of stack
               Convert to a value of correct type:  If name.type == INTEGER generate code to invoke
Java.lang.Integer.parseInt.   If BOOLEAN, invoke java/lang/Boolean.parseBoolean
*/
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		// TODO (see comment )
		Source source = statement_In.source;
		String field = statement_In.name;
		source.visit(this, null);   // put source string on top of stack
		Type fieldType = statement_In.getDec().typeVal;
		String fieldTypeString = "";
		if(fieldType==Type.BOOLEAN)
		{
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);	
			fieldTypeString = "Z";
		}
		else if (fieldType == Type.INTEGER)
		{
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);		
			fieldTypeString = "I";
		}
		else if( fieldType == Type.IMAGE) {
			
		fieldTypeString = 	 ImageSupport.ImageDesc;
		Declaration_Image di = (Declaration_Image) statement_In.getDec();
		if(di.xSize!=null) {
			mv.visitFieldInsn(GETSTATIC, className, field , ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", 
					ImageSupport.getXSig, false);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", 
					"(I)Ljava/lang/Integer;", false);
			mv.visitFieldInsn(GETSTATIC, className, field , ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", 
					ImageSupport.getYSig, false);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", 
					"(I)Ljava/lang/Integer;", false);
		}
		else {
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ACONST_NULL);	
		}

		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage",
				ImageSupport.readImageSig, false);
		}
		mv.visitFieldInsn(PUTSTATIC, className, field, fieldTypeString);		
		return null;
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	/*
	 * If the type is integer or boolean visit the expression to generate
	 *  code to leave its value on top of the stack. Then visit the LHS to generate code to 
	 *  store the top of the stack in the lhs variable.*/
	/*AssignmentStatement ::= Lhs OP_ASSIGN Expression
	 * Lhs::= IDENTIFIER ( LSQUARE LhsSelector RSQUARE | ε )*/
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) 
			throws Exception {
		LHS lhs = statement_Assign.lhs;
		String field = lhs.name;
		Expression expr = statement_Assign.e;
		if(lhs.typeVal==Type.INTEGER || lhs.typeVal == Type.BOOLEAN)
		{
			expr.visit(this, null);
			lhs.visit(this, null); // stores the top of stack in lhs variable
		}
		else if(lhs.typeVal==Type.IMAGE) {
			// store 0 in x
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 1);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 2);
			mv.visitFieldInsn(GETSTATIC, className, field, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitVarInsn(ISTORE, 5);
			// store Ysize in Y
			mv.visitFieldInsn(GETSTATIC, className, field, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitVarInsn(ISTORE, 6);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 1); // x = 0
			Label l5 = new Label();
			mv.visitJumpInsn(GOTO, l5);
			Label l6 = new Label();
			mv.visitLabel(l6); // y =0
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 2);
			Label l7 = new Label();
			mv.visitJumpInsn(GOTO, l7);
			Label l8 = new Label();
			mv.visitLabel(l8);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
			mv.visitVarInsn(ISTORE, 3);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
			mv.visitVarInsn(ISTORE, 4);
			expr.visit(this, null);
			lhs.visit(this, null);

			mv.visitIincInsn(2, 1); // y++
			mv.visitLabel(l7); // y < Y
			mv.visitVarInsn(ILOAD, 2);
			mv.visitVarInsn(ILOAD, 6);
			mv.visitJumpInsn(IF_ICMPLT, l8);
			Label l9 = new Label();
			mv.visitLabel(l9);
			mv.visitIincInsn(1, 1); // x ++
			mv.visitLabel(l5);
			mv.visitVarInsn(ILOAD, 1); // x<X
			mv.visitVarInsn(ILOAD, 5);
			mv.visitJumpInsn(IF_ICMPLT, l6);
		}
		//TODO  (see comment)
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	/*
	 * If LHS.Type  is INTEGER or BOOLEAN, generate code to 
                           store the value on top of the stack in variable name.
                           If LHS.Type is IMAGE, a  pixel is on top of the stack.  
                           Generate code to store it in the image at location (x,y).  
                           (Load the image ref, load x and y, invoke ImageSupport.setPixel)
*/
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		String field = lhs.name;
		if(lhs.typeVal == Type.INTEGER || lhs.typeVal == Type.BOOLEAN)
		{
			String fieldType = lhs.typeVal==Type.INTEGER?"I":"Z";
			mv.visitFieldInsn(PUTSTATIC, className, field, fieldType );		
		}
		else if(lhs.typeVal== Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, field, ImageSupport.ImageDesc);// have to store the ref of object
			mv.visitVarInsn(ILOAD, 1);  // store x
			mv.visitVarInsn(ILOAD, 2);  // store y
			

			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", 
					ImageSupport.setPixelSig, false);
		}
		return null;
	}
	
/*Generate code to display the image (whose ref should be on top of the stack already) on the screen.
 *   Call ImageFrame.makeFrame. 
 *  Note that this method returns a reference to the frame which is not needed, so pop it off the stack*/
	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame", 
				ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
			
		return null;
	}
/*
 * The identifier should contain a reference to a String representing a filename.  
 * Generate code to write the image to the file.  
 * The image reference should already be on the stack, so load the filename and invoke ImageSupport.write.
*/
	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		String field = sink_Ident.name;
		mv.visitFieldInsn(GETSTATIC, className, field,"Ljava/lang/String;" );
		
		//mv.visitLdcInsn(field);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", 
				ImageSupport.writeSig, false);
		return null;
	}
/*Generate code to leave the value of the literal on top of the stack*/
	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
         mv.visitLdcInsn(expression_BooleanLit.value);   
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		String field = expression_Ident.name;
		Type type = expression_Ident.typeVal;
		String fieldtype= "";
		if(type==Type.BOOLEAN) {
			fieldtype = "Z";
		}
		else if(type==Type.FILE || type == Type.URL) {
			fieldtype = "Ljava/lang/String;";
		}
		else if(type==Type.INTEGER) {
			fieldtype = "I";
		}else if(type == Type.IMAGE) {
			fieldtype = "Ljava/awt/image/BufferedImage;";	
		}
		
		mv.visitFieldInsn(GETSTATIC, className, field, fieldtype);
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.typeVal);
		return null;
	}

}
