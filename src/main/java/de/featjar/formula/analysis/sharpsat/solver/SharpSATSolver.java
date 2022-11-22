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
package de.featjar.formula.analysis.sharpsat.solver;

import de.featjar.base.Feat;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.bin.sharpsat.SharpSATBinary;
import de.featjar.formula.analysis.solver.Assumable;
import de.featjar.formula.io.dimacs.DIMACSCNFFormat;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;

public class SharpSATSolver implements de.featjar.formula.analysis.solver.SharpSATSolver {
    private final SharpSATSolverFormula formula;
    private final Assignment assumptions;

    private long timeout = 0;

    public SharpSATSolver(CNF cnf) {
        //final TermMap variables = modelExpression.getTermMap().orElseGet(TermMap::new);
        formula = new SharpSATSolverFormula();
        formula.push(cnf);
        assumptions = new Assignment();
    }

    private CNF simplifyCNF(CNF cnf) {
        final HashSet<Integer> unitClauses = new HashSet<>();
        ArrayList<SortedIntegerList> sortedIntegerLists = cnf.getClauseList();
        for (final SortedIntegerList sortedIntegerList : sortedIntegerLists) {
            if (sortedIntegerList.size() == 1) {
                final int literal = sortedIntegerList.getIntegers()[0];
                if (unitClauses.add(literal) && unitClauses.contains(-literal)) {
                    return null;
                }
            }
        }
//        for (final Pair<Integer, Object> entry : assumptions.get()) {
//            final int variable = entry.getKey();
//            final int literal = (entry.getValue() == Boolean.TRUE) ? variable : -variable;
//            if (unitClauses.add(literal) && unitClauses.contains(-literal)) {
//                return null;
//            }
        //}
        if (!unitClauses.isEmpty()) {
            //final TermMap variables = cnf.getVariableMap();
            int unitClauseCount = 0;
            while (unitClauseCount != unitClauses.size()) {
                unitClauseCount = unitClauses.size();
//                if (unitClauseCount == variables.getVariableCount()) {
//                    return new CNF(new VariableMap());
//                }
                final ArrayList<SortedIntegerList> nonUnitSortedIntegerLists = new ArrayList<>();
                clauseLoop:
                for (final SortedIntegerList sortedIntegerList : sortedIntegerLists) {
                    if (sortedIntegerList.size() > 1) {
                        int[] literals = sortedIntegerList.getIntegers();
                        int deadLiterals = 0;
                        for (int i = 0; i < literals.length; i++) {
                            final int literal = literals[i];
                            if (unitClauses.contains(literal)) {
                                literals = null;
                                continue clauseLoop;
                            } else if (unitClauses.contains(-literal)) {
                                deadLiterals++;
                            }
                        }
                        if (deadLiterals > 0) {
                            final int[] newLiterals = new int[literals.length - deadLiterals];
                            for (int i = 0, j = 0; j < newLiterals.length; i++) {
                                final int literal = literals[i];
                                if (!unitClauses.contains(-literal)) {
                                    newLiterals[j++] = literal;
                                }
                            }
                            if (newLiterals.length > 1) {
                                nonUnitSortedIntegerLists.add(new SortedIntegerList(newLiterals, sortedIntegerList.getOrder(), false));
                            } else if (newLiterals.length == 1) {
                                final int literal = newLiterals[0];
                                if (unitClauses.add(literal) && unitClauses.contains(-literal)) {
                                    return null;
                                }
                            } else {
                                return null;
                            }
                        } else {
                            nonUnitSortedIntegerLists.add(sortedIntegerList);
                        }
                    }
                }
                sortedIntegerLists = nonUnitSortedIntegerLists;
            }

            //final TermMap newVariables = variables.clone();
            //unitClauses.stream().map(Math::abs).forEach(newVariables::removeVariable);

//            if (clauses.isEmpty()) {
//                return new CNF(newVariables);
//            }
            //cnf = new CNF(variables, clauses).adapt(newVariables).orElseThrow();
        }
        return cnf;
    }

    @Override
    public Result<BigInteger> countSolutions() {
        try {
            // final CNF cnf = simplifyCNF(formula.getCNF()); // TODO: variable map not adapted correctly
            final CNF cnf = formula.getCNF();
            if (cnf == null) {
                return Result.of(BigInteger.ZERO);
            }
            if (cnf.getClauseList().isEmpty()) {
                return Result.of(BigInteger.valueOf(2).pow(cnf.getVariableMap().getVariableCount()));
            }
            Feat.extension(SharpSATBinary.class).withTemporaryFile("sharpSATinput", ".dimacs", temp -> {
                try {
                    IO.save(cnf, temp, new DIMACSCNFFormat());
                } catch (IOException e) {
                    return Result.empty(e);
                }

                return Feat.extension(SharpSATBinary.class)
                        .execute("-noCC", "-noIBCP", "-t", String.valueOf(timeout), temp.toString())
                        .map(lines -> lines
                                .findFirst()
                                .map(BigInteger::new)
                                .orElse(BigInteger.ZERO));
            });
        } catch (final Exception e) {
            Feat.log().error(e);
        }
        return Result.empty();
    }

    @Override
    public Result<Boolean> hasSolution() {
        final int comparison = countSolutions().map(c -> c.compareTo(BigInteger.ZERO)).orElse(-1);
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

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public Assumable<?> getAssumptionList() {
        return assumptions;
    }

    @Override
    public void setAssumptionList(Assumable<?> assumptions) throws SolverContradictionException {
        //TODO
    }

    @Override
    public SharpSATSolverFormula getSolverFormula() {
        return formula;
    }
}
