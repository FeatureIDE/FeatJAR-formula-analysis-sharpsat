/*
 * Copyright (C) 2023 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sharpsat.
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
 * See <https://github.com/FeatJAR> for further information.
 */
package de.featjar.formula.analysis.sharpsat;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.ITimeoutDependency;
import de.featjar.formula.analysis.sharpsat.solver.SharpSATSolver;
import de.featjar.formula.structure.formula.IFormula;
import java.time.Duration;
import java.util.List;

public abstract class ASharpSATAnalysis<T> extends AComputation<T> {
    public static final Dependency<IFormula> FORMULA = Dependency.newDependency(IFormula.class);
    public static final Dependency<Duration> TIMEOUT = Dependency.newDependency(Duration.class);

    public ASharpSATAnalysis(IComputation<IFormula> formula, Object... dependencies) {
        super(formula, Computations.of(ITimeoutDependency.DEFAULT_TIMEOUT), dependencies);
    }

    public ASharpSATAnalysis(ComputeSolutionCountSharpSAT other) {
        super(other);
    }

    public SharpSATSolver initializeSolver(List<Object> dependencyList) {
        IFormula formula = FORMULA.get(dependencyList);
        Duration timeout = TIMEOUT.get(dependencyList);
        FeatJAR.log().debug("initializing SAT4J");
        FeatJAR.log().debug(formula);
        SharpSATSolver solver = new SharpSATSolver(formula);
        solver.setTimeout(timeout);
        return solver;
    }
}
