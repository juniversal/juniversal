package cj.writecpp;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import cj.ASTUtil;
import cj.CJException;
import cj.CPPProfile;

public class WriteCPP {
	// Data
	HashMap<Class<? extends ASTNode>, WriteCPPVisitor> m_visitors = new HashMap<Class<? extends ASTNode>, WriteCPPVisitor>();

	public WriteCPP() {

		// Type (class/interface) declaration
		addVisitor(TypeDeclaration.class, new WriteTypeVisitor());

		// Block
		addVisitor(Block.class, new WriteCPPVisitor() {
			@Override
			public void write(ASTNode node, WriteCPPContext context) {
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

		// If statement
		addVisitor(IfStatement.class, new WriteCPPVisitor() {
			@Override
			public void write(ASTNode node, WriteCPPContext context) {
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
		addVisitor(WhileStatement.class, new WriteCPPVisitor() {
			@Override
			public void write(ASTNode node, WriteCPPContext context) {
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
		addVisitor(ReturnStatement.class, new WriteCPPVisitor() {
			@Override
			public void write(ASTNode node, WriteCPPContext context) {
				ReturnStatement returnStatement = (ReturnStatement) node;

				context.matchAndWrite("return");

				context.copySpaceAndComments();
				writeNode(returnStatement.getExpression(), context);

				context.copySpaceAndComments();
				context.matchAndWrite(";");
			}
		});

		// Number literal
		addVisitor(NumberLiteral.class, new WriteCPPVisitor() {
			@Override
			public void write(ASTNode node, WriteCPPContext context) {
				NumberLiteral numberLiteral = (NumberLiteral) node;
				context.matchAndWrite(numberLiteral.getToken());
			}
		});

		// Boolean literal
		addVisitor(BooleanLiteral.class, new WriteCPPVisitor() {
			@Override
			public void write(ASTNode node, WriteCPPContext context) {
				BooleanLiteral booleanLiteral = (BooleanLiteral) node;
				context.matchAndWrite(booleanLiteral.booleanValue() ? "true" : "false");
			}
		});

		// Local variable declaration statement
		addVisitor(VariableDeclarationStatement.class, new WriteCPPVisitor() {
			@Override
			public void write(ASTNode node, WriteCPPContext context) {
				VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;

				// Turn "final" into "const"
				if (ASTUtil.containsFinal(variableDeclarationStatement.modifiers())) {
					context.write("const");
					context.skipModifiers(variableDeclarationStatement.modifiers());

					context.copySpaceAndComments();
				}

				// Write the type
				writeNode(variableDeclarationStatement.getType(), context);

				// Write the variable declaration(s)
				boolean firstFragment = true;
				for (Object fragmentObject : variableDeclarationStatement.fragments()) {
					VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragmentObject;

					context.copySpaceAndComments();
					if (!firstFragment) {
						context.matchAndWrite(",");
						context.copySpaceAndComments();
					}
					writeNode(fragment, context);

					firstFragment = false;
				}

				context.copySpaceAndComments();
				context.matchAndWrite(";");
			}
		});

		// Variable declaration fragment
		addVisitor(VariableDeclarationFragment.class, new WriteCPPVisitor() {
			@Override
			public void write(ASTNode node, WriteCPPContext context) {
				VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) node;

				writeNode(variableDeclarationFragment.getName(), context);

				Expression initializer = variableDeclarationFragment.getInitializer();
				if (initializer != null) {
					context.copySpaceAndComments();
					context.matchAndWrite("=");

					context.copySpaceAndComments();
					writeNode(initializer, context);
				}

				/*
				// Whitespace/comments at the end of the fragment are considered part of the
				// fragment in the AST, so include it here in order to process everything in
				// the node
				// THIS IS NO LONGER TRUE APPARENTLY WITH THE JDT IN 3.6
				context.copySpaceAndComments();
				*/
			}
		});

		// Primitive type
		addVisitor(PrimitiveType.class, new WriteCPPVisitor() {
			@Override
			public void write(ASTNode node, WriteCPPContext context) {
				PrimitiveType primitiveType = (PrimitiveType) node;

				CPPProfile profile = context.getCPPProfile();

				PrimitiveType.Code code = primitiveType.getPrimitiveTypeCode();
				if (code == PrimitiveType.BYTE)
					context.matchAndWrite("byte", profile.getInt8Type());
				else if (code == PrimitiveType.SHORT)
					context.matchAndWrite("short", profile.getInt16Type());
				else if (code == PrimitiveType.CHAR)
					context.matchAndWrite("char", profile.getUnsignedInt32UnicodeCharType());
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
					context.throwSourceNotSupported("unknown primitive type: " + code);
			}
		});

		// Simple name
		addVisitor(SimpleName.class, new WriteCPPVisitor() {
			@Override
			public void write(ASTNode node, WriteCPPContext context) {
				SimpleName simpleName = (SimpleName) node;

				context.matchAndWrite(simpleName.getIdentifier());
			}
		});
	}

	public void writeNode(ASTNode node, WriteCPPContext context) {
		int nodeStartPosition = node.getStartPosition();
		context.assertPositionIs(nodeStartPosition);

		getVisitor(node.getClass()).write(node, context);

		context.assertPositionIs(nodeStartPosition + node.getLength());
	}

	private void addVisitor(Class<? extends ASTNode> clazz, WriteCPPVisitor visitor) {
		m_visitors.put(clazz, visitor);
	}

	static int mem;

	void testit() {
		cj.writecpp.WriteCPP.mem = 3;

		int[][] arr[], barr[][][];
		arr = new int[3][][];
		barr = new int[3][3][][][];
		arr[3][0][0] = 5;
		barr[10][10] = new int[21][][];

		IProject project;
		// JavaCore.create();
		org.eclipse.jdt.internal.compiler.batch.Main main;

	}

	public WriteCPPVisitor getVisitor(Class<? extends ASTNode> clazz) {
		WriteCPPVisitor visitor = m_visitors.get(clazz);
		if (visitor == null)
			throw new CJException("No visitor found for class " + clazz.getName());
		return visitor;
	}
}
