package juniversal.cplusplus.astwriters;

import java.util.HashMap;

import juniversal.JUniversalException;
import juniversal.cplusplus.CPPProfile;
import juniversal.cplusplus.Context;
import juniversal.cplusplus.SourceCopier;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;


public class ASTWriters {
	// Data
	HashMap<Class<? extends ASTNode>, ASTWriter> m_visitors = new HashMap<Class<? extends ASTNode>, ASTWriter>();
	
	public ASTWriters() {

		addDeclarationWriters();

		addStatementWriters();

		addExpressionWriters();

		// Simple name
		addWriter(SimpleName.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				SimpleName simpleName = (SimpleName) node;

				context.matchAndWrite(simpleName.getIdentifier());
			}
		});
	}

	/**
	 * Add visitors for class, method, field, and type declarations.
	 */
	private void addDeclarationWriters() {
		// Type (class/interface) declaration
		addWriter(TypeDeclaration.class, new TypeDeclarationWriter(this));

		// Method declaration (which includes implementation)
		addWriter(MethodDeclaration.class, new MethodDeclarationWriter(this));

		// Field declaration
		addWriter(FieldDeclaration.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
	
				// TODO: Handle final/const
	
				context.skipModifiers(fieldDeclaration.modifiers());
	
				// Write the type
				context.skipSpaceAndComments();
				writeNode(fieldDeclaration.getType(), context);
	
				boolean first = true;
				for (Object fragment : fieldDeclaration.fragments()) {
					VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragment;

					context.copySpaceAndComments();
					if (! first) {
						context.matchAndWrite(",");
						context.copySpaceAndComments();
					}
					writeNode(variableDeclarationFragment, context);
	
					first = false;
				}
	
				context.copySpaceAndComments();
				context.matchAndWrite(";");
			}
		});

		// Variable declaration fragment
		addWriter(VariableDeclarationFragment.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) node;

				// TODO: Handle syntax with extra dimensions on array
				if (variableDeclarationFragment.getExtraDimensions() > 0)
					context.throwSourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

				if (context.isWritingVariableDeclarationNeedingStar())
					context.write("*");

				writeNode(variableDeclarationFragment.getName(), context);
	
				Expression initializer = variableDeclarationFragment.getInitializer();
				if (initializer != null) {
					context.copySpaceAndComments();
					context.matchAndWrite("=");
	
					context.copySpaceAndComments();
					writeNode(initializer, context);
				}
			}
		});

		// Single variable declaration (used in parameter list & catch clauses)
		addWriter(SingleVariableDeclaration.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) node;
				
				// TODO: Handle syntax with extra dimensions on array
				if (singleVariableDeclaration.getExtraDimensions() > 0)
					context.throwSourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");
	
				// TODO: Handle final & varargs
	
				writeNode(singleVariableDeclaration.getType(), context);
	
				context.copySpaceAndComments();
	
				writeNode(singleVariableDeclaration.getName(), context);
	
				context.copySpaceAndComments();
	
				Expression initializer = singleVariableDeclaration.getInitializer();
				if (initializer != null)
					throw new JUniversalException("Unexpected initializer present for SingleVariableDeclaration");
			}
		});

		// Simple type
		addWriter(SimpleType.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				SimpleType simpleType = (SimpleType) node;

				writeNode(simpleType.getName(), context);
			}
		});

		// Array type
		addWriter(ArrayType.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				ArrayType arrayType = (ArrayType) node;

				context.write("array<");
				writeNode(arrayType.getComponentType(), context);
				context.skipSpaceAndComments();
				context.write(">");

				context.match("[");
				context.skipSpaceAndComments();

				context.match("]");
			}
		});

		// Primitive type
		addWriter(PrimitiveType.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				PrimitiveType primitiveType = (PrimitiveType) node;
	
				CPPProfile profile = context.getCPPProfile();
	
				PrimitiveType.Code code = primitiveType.getPrimitiveTypeCode();
				if (code == PrimitiveType.BYTE)
					context.matchAndWrite("byte", profile.getInt8Type());
				else if (code == PrimitiveType.SHORT)
					context.matchAndWrite("short", profile.getInt16Type());
				else if (code == PrimitiveType.CHAR)
					context.matchAndWrite("char", "unichar");
				else if (code == PrimitiveType.INT)
					context.matchAndWrite("int", profile.getInt32Type());
				else if (code == PrimitiveType.LONG) {
					String int64Type = profile.getInt64Type();
					if (int64Type == null)
						context.throwSourceNotSupported("long type isn't supported by default; need to specify target C++ type for 64 bit int");
					context.matchAndWrite("long", int64Type);
				} else if (code == PrimitiveType.FLOAT)
					context.matchAndWrite("float", profile.getFloat32Type());
				else if (code == PrimitiveType.DOUBLE)
					context.matchAndWrite("double", profile.getFloat64Type());
				else if (code == PrimitiveType.BOOLEAN)
					context.matchAndWrite("boolean", "bool");
				else if (code == PrimitiveType.VOID)
					context.matchAndWrite("void", "void");
				else
					context.throwInvalidAST("Unknown primitive type: " + code);
			}
		});
	}

	/**
	 * Add visitors for the different kinds of statements.
	 */
	private void addStatementWriters() {
		// Block
		addWriter(Block.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				Block block = (Block) node;

				context.matchAndWrite("{");

				for (Object statementObject : block.statements()) {
					Statement statement = (Statement) statementObject;
					context.copySpaceAndComments();

					writeNode(statement, context);
				}

				context.copySpaceAndComments();
				context.matchAndWrite("}");
			}
		});
		
		// Empty statement (";")
		addWriter(EmptyStatement.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				context.matchAndWrite(";");
			}
		});

		// Expression statement
		addWriter(ExpressionStatement.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				ExpressionStatement expressionStatement = (ExpressionStatement) node;

				writeNode(expressionStatement.getExpression(), context);
				context.copySpaceAndComments();

				context.matchAndWrite(";");
			}
		});

		// If statement
		addWriter(IfStatement.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				IfStatement ifStatement = (IfStatement) node;

				context.matchAndWrite("if");
				context.copySpaceAndComments();

				context.matchAndWrite("(");
				context.copySpaceAndComments();

				writeNode(ifStatement.getExpression(), context);
				context.copySpaceAndComments();

				context.matchAndWrite(")");
				context.copySpaceAndComments();

				writeNode(ifStatement.getThenStatement(), context);

				Statement elseStatement = ifStatement.getElseStatement();
				if (elseStatement != null) {
					context.copySpaceAndComments();

					context.matchAndWrite("else");
					context.copySpaceAndComments();

					writeNode(elseStatement, context);
				}
			}
		});

		// While statement
		addWriter(WhileStatement.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				WhileStatement whileStatement = (WhileStatement) node;

				context.matchAndWrite("while");
				context.copySpaceAndComments();

				context.matchAndWrite("(");
				context.copySpaceAndComments();

				writeNode(whileStatement.getExpression(), context);
				context.copySpaceAndComments();

				context.matchAndWrite(")");
				context.copySpaceAndComments();

				writeNode(whileStatement.getBody(), context);
			}
		});

		addWriter(ForStatement.class, new ForStatementWriter(this));

		addWriter(WhileStatement.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				WhileStatement whileStatement = (WhileStatement) node;

				context.matchAndWrite("while");
				context.copySpaceAndComments();

				context.matchAndWrite("(");
				context.copySpaceAndComments();

				writeNode(whileStatement.getExpression(), context);
				context.copySpaceAndComments();
				
				context.matchAndWrite(")");
				context.copySpaceAndComments();

				writeNode(whileStatement.getBody(), context);
			}
		});

		// Return statement
		addWriter(ReturnStatement.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				ReturnStatement returnStatement = (ReturnStatement) node;

				context.matchAndWrite("return");
				context.copySpaceAndComments();

				writeNode(returnStatement.getExpression(), context);
				context.copySpaceAndComments();

				context.matchAndWrite(";");
			}
		});

		// Local variable declaration statement
		addWriter(VariableDeclarationStatement.class, new VariableDeclarationWriter(this));

		// Throw statement
		addWriter(ThrowStatement.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				ThrowStatement throwStatement = (ThrowStatement) node;

				context.matchAndWrite("throw");
				context.copySpaceAndComments();

				writeNode(throwStatement.getExpression(), context);
				context.copySpaceAndComments();
				
				context.matchAndWrite(";");
			}
		});

		addWriter(ConstructorInvocation.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				context.throwSourceNotSupported("Delegating constructors aren't currently supported; for now you have to change the code to not use them (e.g. by adding an init method)");
			}
		});
	}

	/**
	 * Add visitors for the different kinds of expressions.
	 */
	private void addExpressionWriters() {
		
		// Assignment expression
		addWriter(Assignment.class, new AssignmentWriter(this));

		// Method invocation
		addWriter(MethodInvocation.class, new MethodInvocationWriter(this));
		
		// Class instance creation
		addWriter(ClassInstanceCreation.class, new ClassInstanceCreationWriter(this));

		// Array creation
		addWriter(ArrayCreation.class, new ArrayCreationWriter(this));

		// Variable declaration expression (used in a for statement)
		addWriter(VariableDeclarationExpression.class, new VariableDeclarationWriter(this));
		
		// Infix expression
		addWriter(InfixExpression.class, new InfixExpressionWriter(this));

		// Prefix expression
		addWriter(PrefixExpression.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				PrefixExpression prefixExpression = (PrefixExpression) node;

				PrefixExpression.Operator operator = prefixExpression.getOperator();
				if (operator == PrefixExpression.Operator.INCREMENT)
					context.matchAndWrite("++");
				else if (operator == PrefixExpression.Operator.DECREMENT)
					context.matchAndWrite("--");
				else if (operator == PrefixExpression.Operator.PLUS)
					context.matchAndWrite("+");
				else if (operator == PrefixExpression.Operator.MINUS)
					context.matchAndWrite("-");
				else if (operator == PrefixExpression.Operator.COMPLEMENT)
					context.matchAndWrite("~");
				else if (operator == PrefixExpression.Operator.NOT)
					context.matchAndWrite("!");
				else context.throwInvalidAST("Unknown prefix operator type: " + operator);
				context.copySpaceAndComments();
				
				writeNode(prefixExpression.getOperand(), context);
			}
		});

		// Postfix expression
		addWriter(PostfixExpression.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				PostfixExpression postfixExpression = (PostfixExpression) node;
				
				writeNode(postfixExpression.getOperand(), context);
				context.copySpaceAndComments();
				
				PostfixExpression.Operator operator = postfixExpression.getOperator();
				if (operator == PostfixExpression.Operator.INCREMENT)
					context.matchAndWrite("++");
				else if (operator == PostfixExpression.Operator.DECREMENT)
					context.matchAndWrite("--");
				else context.throwInvalidAST("Unknown postfix operator type: " + operator);
			}
		});

		// instanceof expression
		addWriter(InstanceofExpression.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				InstanceofExpression instanceofExpression = (InstanceofExpression) node;

				context.write("INSTANCEOF(");

				Expression expression = instanceofExpression.getLeftOperand();
				writeNode(expression, context);
				context.skipSpaceAndComments();

				context.match("instanceof");
				context.skipSpaceAndComments();

				Type type = instanceofExpression.getRightOperand();
				writeNode(type, context);

				context.write(")");
			}
		});

		// this 
		addWriter(ThisExpression.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				ThisExpression thisExpression = (ThisExpression) node;

				// TODO: Handle qualified this expressions; probably need to do from parent invoking
				// node & disallow qualified this accesses if not field reference / method
				// invocation; it's allowed otherwise in Java but I don't think it does anything
				// MyClass.this.   -->   this->MyClass::
				if (thisExpression.getQualifier() != null)
					throw new JUniversalException("Qualified this expression isn't supported yet");

				context.matchAndWrite("this");
			}
		});
	
		// Field access 
		addWriter(FieldAccess.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				FieldAccess fieldAccess = (FieldAccess) node;
				
				writeNode(fieldAccess.getExpression(), context);
				context.copySpaceAndComments();

				context.matchAndWrite(".", "->");

				writeNode(fieldAccess.getName(), context);
			}
		});
	
		// Array access 
		addWriter(ArrayAccess.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				ArrayAccess arrayAccess = (ArrayAccess) node;
				
				writeNode(arrayAccess.getArray(), context);
				context.copySpaceAndComments();

				context.matchAndWrite("[");
				context.copySpaceAndComments();

				writeNode(arrayAccess.getIndex(), context);
				context.copySpaceAndComments();

				context.matchAndWrite("]");
			}
		});
	
		// Qualfied name 
		addWriter(QualifiedName.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				QualifiedName qualifiedName = (QualifiedName) node;

				// TODO: Figure out the other cases where this can occur & make them all correct 

				writeNode(qualifiedName.getQualifier(), context);
				context.copySpaceAndComments();

				context.matchAndWrite(".", "->");

				writeNode(qualifiedName.getName(), context);
			}
		});

		// Parenthesized expression
		addWriter(ParenthesizedExpression.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				ParenthesizedExpression parenthesizedExpression= (ParenthesizedExpression) node;
				
				context.matchAndWrite("(");
				context.copySpaceAndComments();
				
				writeNode(parenthesizedExpression.getExpression(), context);
				context.copySpaceAndComments();
	
				context.matchAndWrite(")");
			}
		});

		// Cast expression
		addWriter(CastExpression.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				CastExpression castExpression = (CastExpression) node;

				context.matchAndWrite("(", "static_cast<");
				context.copySpaceAndComments();

				writeNode(castExpression.getType(), context);
				context.copySpaceAndComments();

				context.matchAndWrite(")", ">(");
				context.copySpaceAndComments();

				writeNode(castExpression.getExpression(), context);

				context.write(")");
			}
		});
	
		// Number literal
		addWriter(NumberLiteral.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				NumberLiteral numberLiteral = (NumberLiteral) node;
				context.matchAndWrite(numberLiteral.getToken());
			}
		});
	
		// Boolean literal
		addWriter(BooleanLiteral.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				BooleanLiteral booleanLiteral = (BooleanLiteral) node;
				context.matchAndWrite(booleanLiteral.booleanValue() ? "true" : "false");
			}
		});

		// Null literal
		addWriter(NullLiteral.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				context.matchAndWrite("null", "NULL");
			}
		});

		// String literal
		addWriter(StringLiteral.class, new ASTWriter() {
			@Override
			public void write(ASTNode node, Context context) {
				StringLiteral stringLiteral = (StringLiteral) node;

				context.write("new String(" + stringLiteral.getEscapedValue() + "L)");
				context.match(stringLiteral.getEscapedValue());
			}
		});
	}

	private static final boolean VALIDATE_CONTEXT_POSITION = true;
	public void writeNode(ASTNode node, Context context) {

		int nodeStartPosition;
		if (VALIDATE_CONTEXT_POSITION) {
			nodeStartPosition = node.getStartPosition();

			// If the node starts with Javadoc (true for method declarations with Javadoc before
			// them), then skip past it; the caller is always expected to handle any comments,
			// Javadoc or not, coming before the start of code proper for the node
			SourceCopier sourceCopier = context.getSourceCopier();
			if (sourceCopier.getSourceCharAt(nodeStartPosition) == '/'
					&& sourceCopier.getSourceCharAt(nodeStartPosition + 1) == '*'
					&& sourceCopier.getSourceCharAt(nodeStartPosition + 1) == '*')
				context.assertPositionIs(sourceCopier.skipSpaceAndComments(nodeStartPosition));
			else context.assertPositionIs(nodeStartPosition);
		}

		getVisitor(node.getClass()).write(node, context);

		if (VALIDATE_CONTEXT_POSITION)
			context.assertPositionIs(nodeStartPosition + node.getLength());
	}

	private void addWriter(Class<? extends ASTNode> clazz, ASTWriter visitor) {
		m_visitors.put(clazz, visitor);
	}

	static int mem;

	void testit() {
		juniversal.cplusplus.astwriters.ASTWriters.mem = 3;

		int[][] arr[], barr[][][];
		arr = new int[3][][];
		barr = new int[3][3][][][];
		arr[3][0][0] = 5;
		barr[10][10] = new int[21][][];

		IProject project;
		// JavaCore.create();
		org.eclipse.jdt.internal.compiler.batch.Main main;

	}

	public ASTWriter getVisitor(Class<? extends ASTNode> clazz) {
		ASTWriter visitor = m_visitors.get(clazz);
		if (visitor == null)
			throw new JUniversalException("No visitor found for class " + clazz.getName());
		return visitor;
	}
}
