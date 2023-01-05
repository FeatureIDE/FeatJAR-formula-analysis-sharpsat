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
package de.featjar.formula.analysis.sharpsat;

import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.FutureResult;

/**
 * Counts the number of valid solutions to a formula.
 *
 * @author Sebastian Krieter
 */
public class HasSolutionAnalysis extends SharpSATSolverAnalysis<Boolean> {
    public HasSolutionAnalysis(IComputation<CNF> inputComputation) {
        super(inputComputation);
    }

    public HasSolutionAnalysis(IComputation<CNF> inputComputation, Assignment assumptions, long timeoutInMs, long randomSeed) {
        super(inputComputation, assumptions, timeoutInMs, randomSeed);
    }

    @Override
    public FutureResult<Boolean> compute() {
        return initializeSolver().thenComputeResult(((solver, progress) -> {
            // TODO: log output
            return solver.hasSolution();
        }));
    }
}
