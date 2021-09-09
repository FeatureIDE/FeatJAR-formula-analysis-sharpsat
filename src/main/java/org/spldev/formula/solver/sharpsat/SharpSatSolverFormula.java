/* -----------------------------------------------------------------------------
 * Formula-Analysis-SharpSat Lib - Library to analyze propositional formulas with SharpSAT.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Formula-Analysis-SharpSat Lib.
 * 
 * Formula-Analysis-SharpSat Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Formula-Analysis-SharpSat Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Formula-Analysis-SharpSat Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/formula-analysis-sharpsat> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.formula.solver.sharpsat;

import java.util.*;

import org.spldev.formula.clauses.*;
import org.spldev.formula.expression.*;
import org.spldev.formula.expression.atomic.literal.*;
import org.spldev.formula.solver.*;

/**
 * Formula for {@link SharpSatSolver}.
 *
 * @author Sebastian Krieter
 */
public class SharpSatSolverFormula extends AbstractDynamicFormula<LiteralList> {

	public SharpSatSolverFormula(VariableMap variableMap) {
		super(variableMap);
	}

	@Override
	public List<LiteralList> push(Formula formula) throws RuntimeContradictionException {
		final ClauseList clauses = FormulaToCNF.convert(formula, variableMap).getClauses();
		clauses.forEach(constraints::add);
		return clauses;
	}

	@Override
	public void clear() {
		constraints.clear();
	}

	public CNF getCNF() {
		return new CNF(variableMap, constraints);
	}

}
