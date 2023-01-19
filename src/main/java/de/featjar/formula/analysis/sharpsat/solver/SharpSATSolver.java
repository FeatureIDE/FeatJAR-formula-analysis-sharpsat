/*
 * Copyright (C) 2023 Sebastian Krieter
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
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sharpsat> for further information.
 */
package de.featjar.formula.analysis.sharpsat.solver;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.ITimeoutDependency;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.bin.sharpsat.SharpSATBinary;
import de.featjar.formula.analysis.ISolver;
import de.featjar.formula.io.dimacs.DIMACSFormulaFormat;
import de.featjar.formula.structure.formula.IFormula;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Objects;

public class SharpSATSolver implements ISolver {
    protected final IFormula formula;
    protected Duration timeout = ITimeoutDependency.DEFAULT_TIMEOUT;
    protected boolean isTimeoutOccurred;

    public SharpSATSolver(IFormula formula) { // todo: use boolean clause list input
        this.formula = formula;
    }

    public IFormula getFormula() {
        return formula;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        Objects.requireNonNull(timeout);
        FeatJAR.log().debug("setting timeout to " + timeout);
        this.timeout = timeout;
    }

    public boolean isTimeoutOccurred() {
        return isTimeoutOccurred;
    }

    public Result<BigInteger> countSolutions() {
        try {
            // final CNF cnf = simplifyCNF(formula.getCNF()); // TODO: variable map not adapted correctly
//            if (clauseList.isEmpty()) {
//                return Result.of(BigInteger.valueOf(2).pow(cnf.getVariableMap().getVariableCount()));
//            }
            return FeatJAR.extension(SharpSATBinary.class).withTemporaryFile("sharpSATinput", ".dimacs", temp -> {
                try {
                    IO.save(formula, temp, new DIMACSFormulaFormat());
                } catch (IOException e) {
                    return Result.empty(e);
                }
                return FeatJAR.extension(SharpSATBinary.class)
                        .getProcess("-noCC", "-noIBCP", "-t", String.valueOf(timeout.toSeconds()), temp.toString())
                        .get()
                        .flatMap(lines -> lines.isEmpty() ? Result.empty() : Result.of(new BigInteger(lines.get(0))));
            });
        } catch (final Exception e) {
            FeatJAR.log().error(e);
        }
        return Result.empty();
    }

    public Result<Boolean> hasSolution() {
        final int comparison =
                countSolutions().map(c -> c.compareTo(BigInteger.ZERO)).orElse(-1);
        switch (comparison) {
            case -1:
                return Result.empty();
            case 0:
                return Result.of(false);
            case 1:
                return Result.of(true);
            default:
                throw new IllegalStateException(String.valueOf(comparison));
        }
    }

//    private BooleanClauseList simplifyCNF(BooleanClauseList clauseList) {
//        final HashSet<Integer> unitClauses = new HashSet<>();
//        for (final BooleanClause clause : clauseList) {
//            if (clause.size() == 1) {
//                final int literal = clause.get()[0];
//                if (unitClauses.add(literal) && unitClauses.contains(-literal)) {
//                    return null;
//                }
//            }
//        }
//        //        for (final Pair<Integer, Object> entry : assumptions.get()) {
//        //            final int variable = entry.getKey();
//        //            final int literal = (entry.getValue() == Boolean.TRUE) ? variable : -variable;
//        //            if (unitClauses.add(literal) && unitClauses.contains(-literal)) {
//        //                return null;
//        //            }
//        // }
//        if (!unitClauses.isEmpty()) {
//            // final TermMap variables = cnf.getVariableMap();
//            int unitClauseCount = 0;
//            while (unitClauseCount != unitClauses.size()) {
//                unitClauseCount = unitClauses.size();
//                //                if (unitClauseCount == variables.getVariableCount()) {
//                //                    return new CNF(new VariableMap());
//                //                }
//                final ArrayList<SortedIntegerList> nonUnitSortedIntegerLists = new ArrayList<>();
//                clauseLoop:
//                for (final SortedIntegerList sortedIntegerList : sortedIntegerLists) {
//                    if (sortedIntegerList.size() > 1) {
//                        int[] literals = sortedIntegerList.getIntegers();
//                        int deadLiterals = 0;
//                        for (int i = 0; i < literals.length; i++) {
//                            final int literal = literals[i];
//                            if (unitClauses.contains(literal)) {
//                                literals = null;
//                                continue clauseLoop;
//                            } else if (unitClauses.contains(-literal)) {
//                                deadLiterals++;
//                            }
//                        }
//                        if (deadLiterals > 0) {
//                            final int[] newLiterals = new int[literals.length - deadLiterals];
//                            for (int i = 0, j = 0; j < newLiterals.length; i++) {
//                                final int literal = literals[i];
//                                if (!unitClauses.contains(-literal)) {
//                                    newLiterals[j++] = literal;
//                                }
//                            }
//                            if (newLiterals.length > 1) {
//                                nonUnitSortedIntegerLists.add(
//                                        new SortedIntegerList(newLiterals, sortedIntegerList.getOrder(), false));
//                            } else if (newLiterals.length == 1) {
//                                final int literal = newLiterals[0];
//                                if (unitClauses.add(literal) && unitClauses.contains(-literal)) {
//                                    return null;
//                                }
//                            } else {
//                                return null;
//                            }
//                        } else {
//                            nonUnitSortedIntegerLists.add(sortedIntegerList);
//                        }
//                    }
//                }
//                sortedIntegerLists = nonUnitSortedIntegerLists;
//            }
//
//            // final TermMap newVariables = variables.clone();
//            // unitClauses.stream().map(Math::abs).forEach(newVariables::removeVariable);
//
//            //            if (clauses.isEmpty()) {
//            //                return new CNF(newVariables);
//            //            }
//            // cnf = new CNF(variables, clauses).adapt(newVariables).orElseThrow();
//        }
//        return cnf;
//    }
}
