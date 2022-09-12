/*
 * Copyright (C) 2022 Sebastian Krieter
 *
 * This file is part of formula-analysis-sharpsat.
 *
 * formula-analysis-sharpsat is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sharpsat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sharpsat. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sharpsat> for further information.
 */
package de.featjar.analysis.sharpsat;

import de.featjar.formula.analysis.Analysis;
import de.featjar.analysis.sharpsat.solver.SharpSATSolver;
import de.featjar.formula.structure.Formula;

/**
 * Base class for analyses using a {@link SharpSATSolver}.
 *
 * @param <T> the type of the analysis result.
 *
 * @author Sebastian Krieter
 */
public abstract class SharpSatSolverAnalysis<T> extends Analysis<T, SharpSATSolver, Formula> {

    protected int timeout = 30;

    public SharpSatSolverAnalysis() {
        solverInputComputation = FormulaComputation.empty();
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    protected SharpSATSolver createSolver(Formula input) {
        return new SharpSATSolver(input);
    }

    @Override
    protected void prepareSolver(SharpSATSolver solver) {
        super.prepareSolver(solver);
        solver.setTimeout(timeout);
    }
}
