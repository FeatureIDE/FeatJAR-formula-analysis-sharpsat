/*
 * Copyright (C) 2023 Sebastian Krieter
 *
 * This file is part of FeatJAR-formula-analysis-sat4j.
 *
 * formula-analysis-sat4j is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sat4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sat4j. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sat4j> for further information.
 */
package de.featjar.formula.analysis.sharpsat;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.*;
import de.featjar.formula.analysis.bool.BooleanClauseList;
import de.featjar.formula.analysis.sharpsat.solver.SharpSATSolver;
import de.featjar.formula.structure.formula.IFormula;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public abstract class ASharpSATAnalysis<T> extends AComputation<T>
        implements IAnalysis<IFormula, T>,
                ITimeoutDependency {
    protected static final Dependency<IFormula> FORMULA = newRequiredDependency();
    protected static final Dependency<Duration> TIMEOUT = newOptionalDependency(ITimeoutDependency.DEFAULT_TIMEOUT);

    public ASharpSATAnalysis(IComputation<IFormula> formula, Dependency<?>... dependencies) {
        List<Dependency<?>> dependenciesList = new ArrayList<>();
        dependenciesList.add(FORMULA);
        dependenciesList.add(TIMEOUT);
        dependenciesList.addAll(List.of(dependencies));
        dependOn(dependenciesList);
        setInput(formula);
    }

    @Override
    public Dependency<IFormula> getInputDependency() {
        return FORMULA;
    }

    @Override
    public Dependency<Duration> getTimeoutDependency() {
        return TIMEOUT;
    }

    public SharpSATSolver initializeSolver(DependencyList dependencyList) {
        IFormula formula = dependencyList.get(FORMULA);
        Duration timeout = dependencyList.get(TIMEOUT);
        FeatJAR.log().debug("initializing SAT4J");
        //                    Feat.log().debug(clauseList.toValue().get());
        //                    Feat.log().debug("assuming " +
        // assumedAssignment.toValue(clauseList.getVariableMap()).getAndLogProblems());
        //                    Feat.log().debug("assuming " + assumedClauseList.toValue().get());
        //                    Feat.log().debug(clauseList.getVariableMap());
        FeatJAR.log().debug(formula);
        SharpSATSolver solver = new SharpSATSolver(formula);
        solver.setTimeout(timeout);
        return solver;
    }
}
