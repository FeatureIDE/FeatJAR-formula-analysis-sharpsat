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

import de.featjar.bin.sharpsat.SharpSatBinary;
import de.featjar.formula.clauses.CNF;
import de.featjar.formula.clauses.LiteralList;
import de.featjar.formula.io.dimacs.DIMACSFormatCNF;
import de.featjar.formula.structure.Expression;
import de.featjar.formula.structure.assignment.Assignment;
import de.featjar.formula.structure.assignment.VariableAssignment;
import de.featjar.formula.tmp.TermMap;
import de.featjar.base.data.Pair;
import de.featjar.base.io.IO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

public class SharpSATSolver implements de.featjar.formula.analysis.solver.SharpSATSolver {

    public static final BigInteger INVALID_COUNT = BigInteger.valueOf(-1);

    private final SharpSatSolverFormula formula;
    private final VariableAssignment assumptions;

    private final String[] command = new String[6];

    private long timeout = 0;

    public SharpSATSolver(Expression modelExpression) {
        final TermMap variables = modelExpression.getTermMap().orElseGet(TermMap::new);
        formula = new SharpSatSolverFormula(variables);
        modelExpression.getChildren().stream().map(c -> (Expression) c).forEach(formula::push);
        assumptions = new VariableAssignment(variables);
        command[0] = new SharpSatBinary().getPath().toString();
        command[1] = "-noCC";
        command[2] = "-noIBCP";
        command[3] = "-t";
        command[4] = String.valueOf(timeout);
    }

    private CNF simplifyCNF(CNF cnf) {
        final HashSet<Integer> unitClauses = new HashSet<>();
        ArrayList<LiteralList> clauses = cnf.getClauses();
        for (final LiteralList clause : clauses) {
            if (clause.size() == 1) {
                final int literal = clause.getLiterals()[0];
                if (unitClauses.add(literal) && unitClauses.contains(-literal)) {
                    return null;
                }
            }
        }
        for (final Pair<Integer, Object> entry : assumptions.getAll()) {
            final int variable = entry.getKey();
            final int literal = (entry.getValue() == Boolean.TRUE) ? variable : -variable;
            if (unitClauses.add(literal) && unitClauses.contains(-literal)) {
                return null;
            }
        }
        if (!unitClauses.isEmpty()) {
            final TermMap variables = cnf.getVariableMap();
            int unitClauseCount = 0;
            while (unitClauseCount != unitClauses.size()) {
                unitClauseCount = unitClauses.size();
                if (unitClauseCount == variables.getVariableCount()) {
                    return new CNF(new TermMap());
                }
                final ArrayList<LiteralList> nonUnitClauses = new ArrayList<>();
                clauseLoop:
                for (final LiteralList clause : clauses) {
                    if (clause.size() > 1) {
                        int[] literals = clause.getLiterals();
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
                                nonUnitClauses.add(new LiteralList(newLiterals, clause.getOrder(), false));
                            } else if (newLiterals.length == 1) {
                                final int literal = newLiterals[0];
                                if (unitClauses.add(literal) && unitClauses.contains(-literal)) {
                                    return null;
                                }
                            } else {
                                return null;
                            }
                        } else {
                            nonUnitClauses.add(clause);
                        }
                    }
                }
                clauses = nonUnitClauses;
            }

            final TermMap newVariables = variables.clone();
            unitClauses.stream().map(Math::abs).forEach(newVariables::removeVariable);

            if (clauses.isEmpty()) {
                return new CNF(newVariables);
            }
            cnf = new CNF(variables, clauses).adapt(newVariables).orElseThrow();
        }
        return cnf;
    }

    @Override
    public BigInteger countSolutions() {
        try {
            // final CNF cnf = simplifyCNF(formula.getCNF()); // variable map not adapted correctly
            final CNF cnf = formula.getCNF();
            if (cnf == null) {
                return BigInteger.ZERO;
            }
            if (cnf.getClauses().isEmpty()) {
                return BigInteger.valueOf(2).pow(cnf.getVariableMap().getVariableCount());
            }
            final Path tempFile = Files.createTempFile("sharpSATinput", ".dimacs");
            try {
                IO.save(cnf, tempFile, new DIMACSFormatCNF());

                command[command.length - 1] = tempFile.toString();
                final ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = null;
                try {
                    process = processBuilder.start();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    final int exitValue = process.waitFor();
                    if (exitValue == 0) {
                        process = null;
                        final BigInteger result =
                                reader.lines().findFirst().map(BigInteger::new).orElse(BigInteger.ZERO);
                        return result;
                    } else {
                        return INVALID_COUNT;
                    }
                } finally {
                    if (process != null) {
                        process.destroyForcibly();
                    }
                }
            } finally {
                Files.deleteIfExists(tempFile);
            }
        } catch (final Exception e) {
            Feat.log().error(e);
        }
        return INVALID_COUNT;
    }

    @Override
    public SatResult hasSolution() {
        final int comparision = countSolutions().compareTo(BigInteger.ZERO);
        switch (comparision) {
            case -1:
                return SatResult.TIMEOUT;
            case 0:
                return SatResult.FALSE;
            case 1:
                return SatResult.TRUE;
            default:
                throw new IllegalStateException(String.valueOf(comparision));
        }
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public Assignment getAssumptions() {
        return assumptions;
    }

    @Override
    public SharpSatSolverFormula getDynamicFormula() {
        return formula;
    }

    @Override
    public TermMap getVariables() {
        return formula.getVariableMap();
    }
}
