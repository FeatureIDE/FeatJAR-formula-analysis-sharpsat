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
package org.spldev.formula.analysis.sharpsat;

import org.spldev.formula.analysis.*;
import org.spldev.formula.expression.*;
import org.spldev.formula.solver.sharpsat.*;

/**
 * Base class for analyses using a {@link SharpSatSolver}.
 *
 * @param <T> Type of the analysis result.
 *
 * @author Sebastian Krieter
 */
public abstract class SharpSatSolverAnalysis<T> extends AbstractAnalysis<T, SharpSatSolver, Formula> {

	protected int timeout = 30;

	public SharpSatSolverAnalysis() {
		super();
		solverInputProvider = FormulaProvider.empty();
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	protected SharpSatSolver createSolver(Formula input) {
		return new SharpSatSolver(input);
	}

	@Override
	protected void prepareSolver(SharpSatSolver solver) {
		super.prepareSolver(solver);
		solver.setTimeout(timeout);
	}

}
