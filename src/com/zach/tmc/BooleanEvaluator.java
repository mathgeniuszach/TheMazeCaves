package com.zach.tmc;

/**
 * The <code>BooleanEvaluator</code> class evaluates a string expression to see
 * if it is true or false. "AND" and "OR" are written literally, unlike the
 * operators "&amp;" and "|". It does not short-circuit.
 * 
 * @author Zach K
 */
public class BooleanEvaluator {
	
	/**
	 * Evaluates a string expression using to see if it is true or false.
	 * 
	 * @param expression
	 *            The expression to evaluate.
	 * @return The answer of the expression.
	 * @throws IllegalArgumentException
	 *             If there are uneven parenthesis.
	 * @throws StringIndexOutOfBoundsException
	 *             If a in set of parenthesis the closing one. is before the
	 *             opening one.
	 * @see #miniEval(StringBuilder, int, int, boolean)
	 */
	public static boolean eval(String expression) throws IllegalArgumentException, StringIndexOutOfBoundsException {
		// Create a StringBuilder out of the expression that can be changed.
		StringBuilder expressionPiece = new StringBuilder(expression);
		
		// Check for mini-expressions inside of parenthesis.
		int open = expressionPiece.lastIndexOf("(");
		int closed = expressionPiece.indexOf(")", open);
		while (open != -1 || closed != -1) {
			// While there are parenthesis, evaluate the expression inside them.
			if (open == -1 || closed == -1) {
				throw new IllegalArgumentException("Uneven set of parenthesis");
			}
			
			miniEval(expressionPiece, open + 1, closed, true);
			
			open = expressionPiece.indexOf("(");
			closed = expressionPiece.indexOf(")", open);
		}
		
		// Evaluate the final expression.
		miniEval(expressionPiece, 0, expressionPiece.length(), false);
		return Boolean.valueOf(expressionPiece.toString());
	}
	
	/**
	 * Evaluates a part of a expression and updates that spot. It does not support
	 * multiple sets of parenthesis. A side note is that since this uses
	 * {@link Boolean#valueOf(boolean)}, any value other than "true" (and incorrect
	 * syntax) renders as false.
	 * 
	 * @param expression
	 *            The expression to take a part of and update.
	 * @param start
	 *            The start of the expression.
	 * @param end
	 *            The end of the expression.
	 * @param parenthesis
	 *            Whether or not this expression has parenthesis around it that need
	 *            to be deleted.
	 * @see #eval(String)
	 */
	public static void miniEval(StringBuilder expression, int start, int end, boolean parenthesis) {
		// Split the expression into a series of "AND" gates.
		String[] andGates = expression.substring(start, end).split("OR");
		// Evaluate the "AND" gates. If any are true, than the larger "OR" gate is true.
		boolean isOrTrue = false;
		for (String andGate : andGates) {
			// Split each "AND" gate into a series of values.
			String[] values = andGate.split("AND");
			// Evaluate the values. If any are false, than the "AND" gate is false.
			boolean isAndTrue = true;
			for (String value : values) {
				value = value.trim();
				if (value.startsWith("!")) {
					// In the case that there is a '!' at the start of each value, the value is inverted.
					if (Boolean.valueOf(value.substring(1))) {
						isAndTrue = false;
					}
				} else {
					if (!Boolean.valueOf(value)) {
						isAndTrue = false;
					}
				}
			}
			// If the single "AND" gate is true, then that means the larger "OR" gate is true. This does not end the loop.
			if (isAndTrue) {
				isOrTrue = true;
			}
		}
		
		// This part of the code replaces the part of the expression specified with the "OR" gate's value.
		if (parenthesis) {
			// In the case of parenthesis, the code also replaces those parenthesis with the expression.
			expression.replace(start - 1, end + 1, String.valueOf(isOrTrue));
		} else {
			expression.replace(start, end, String.valueOf(isOrTrue));
		}
	}
}
