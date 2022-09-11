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
package de.featjar.analysis.sharpsat.solver;

import de.featjar.formula.analysis.solver.AbstractDynamicFormula;
import de.featjar.formula.analysis.solver.RuntimeContradictionException;
import de.featjar.formula.clauses.CNF;
import de.featjar.formula.clauses.ClauseList;
import de.featjar.formula.clauses.FormulaToCNF;
import de.featjar.formula.clauses.LiteralList;
import de.featjar.formula.structure.Formula;
import de.featjar.formula.structure.atomic.literal.VariableMap;
import java.util.List;

/**
 * Formula for {@link SharpSATSolver}.
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
