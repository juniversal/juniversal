package juniversal;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class ASTUtil {

	public static TypeDeclaration getFirstTypeDeclaration(CompilationUnit compilationUnit) {
		return (TypeDeclaration) compilationUnit.types().get(0);
	}

	public static Block getFirstMethodBlock(CompilationUnit compilationUnit) {
		return getFirstTypeDeclaration(compilationUnit).getMethods()[0].getBody();
	}

	/**
	 * Returns true if the list of extended modifiers (modifiers & annotations) includes "final".
	 * 
	 * @param extendedModifiers
	 *            extended modifiers to check
	 * @return true if and only if list contains "final"
	 */
	public static boolean containsFinal(List<?> extendedModifiers) {
		for (Object extendedModifierObject : extendedModifiers) {
			IExtendedModifier extendedModifier = (IExtendedModifier) extendedModifierObject;
			if (extendedModifier.isModifier() && ((Modifier) extendedModifier).isFinal())
				return true;
		}
		return false;
	}

	/**
	 * Returns true if the list of extended modifiers (modifiers & annotations) includes "static".
	 * 
	 * @param extendedModifiers
	 *            extended modifiers to check
	 * @return true if and only if list contains "static"
	 */
	public static boolean containsStatic(List<?> extendedModifiers) {
		for (Object extendedModifierObject : extendedModifiers) {
			IExtendedModifier extendedModifier = (IExtendedModifier) extendedModifierObject;
			if (extendedModifier.isModifier() && ((Modifier) extendedModifier).isStatic())
				return true;
		}
		return false;
	}

	/**
	 * Determines the access modifier specified in list of modifiers. If no access modifier is
	 * specified, the default access of Package is returned.
	 * 
	 * @param extendedModifiers
	 *            extended modifiers to check
	 * @return AccessModifier specified or Package by default
	 */
	public static AccessLevel getAccessModifier(List<?> extendedModifiers) {
		for (Object extendedModifierObject : extendedModifiers) {
			IExtendedModifier extendedModifier = (IExtendedModifier) extendedModifierObject;
			if (extendedModifier.isModifier()) {
				Modifier modifier = (Modifier) extendedModifier;
				if (modifier.isPublic())
					return AccessLevel.PUBLIC;
				else if (modifier.isProtected())
					return AccessLevel.PROTECTED;
				else if (modifier.isPrivate())
					return AccessLevel.PRIVATE;
			}
		}

		return AccessLevel.PACKAGE;
	}

	/**
	 * Returns the position following the last character in the node; just a shortcut for adding the
	 * length to the start position. Note that the end position may be (one) past the end of the
	 * source.
	 * 
	 * @param node
	 *            ASTNode in question
	 * @return last position in the node + 1
	 */
	public static int getEndPosition(ASTNode node) {
		return node.getStartPosition() + node.getLength();
	}

	public static String simpleNameFromQualifiedName(String qualifiedName) {
		int lastPeriodIndex = qualifiedName.lastIndexOf('.');
		if (lastPeriodIndex == -1)
			return qualifiedName;
		else return qualifiedName.substring(lastPeriodIndex) + 1;
	}
}
