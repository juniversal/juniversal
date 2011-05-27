package cj;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class ASTUtil {

	public static Block getFirstMethodBlock(CompilationUnit compilationUnit) {
		TypeDeclaration clazz = (TypeDeclaration) compilationUnit.types().get(0);
		return clazz.getMethods()[0].getBody();
	}

	/**
	 * Returns true if the list of extended modifiers (modifiers & annotations) includes "final".
	 * 
	 * @param extendedModifiers extended modifiers to check
	 * @return true if and only if list contains "final"
	 */
	public static boolean containsFinal(List<?> extendedModifiers) {
		for (Object extendedModifierObject : extendedModifiers) {
			IExtendedModifier extendedModifier = (IExtendedModifier) extendedModifierObject;
			if (extendedModifier.isModifier() &&
					((Modifier) extendedModifier).isFinal())
				return true;
		}
		return false;
	}

	/**
	 * Returns the position following the last character in the node; just a shortcut for adding the length
	 * to the start position.  Note that the end position may be (one) past the end of the source.
	 *
	 * @param node ASTNode in question
	 * @return last position in the node + 1
	 */
	public static int getEndPosition(ASTNode node) {
		return node.getStartPosition() + node.getLength();
	}
}
