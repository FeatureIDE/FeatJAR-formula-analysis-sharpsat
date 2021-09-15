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

import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import org.spldev.formula.clauses.*;
import org.spldev.formula.expression.*;
import org.spldev.formula.expression.atomic.*;
import org.spldev.formula.expression.atomic.literal.*;
import org.spldev.formula.expression.term.*;
import org.spldev.formula.io.*;
import org.spldev.util.io.*;
import org.spldev.util.logging.*;

public class SharpSatSolver implements org.spldev.formula.solver.SharpSatSolver {

	public static final BigInteger INVALID_COUNT = BigInteger.valueOf(-1);

	private SharpSatSolverFormula formula;
	private VariableAssignment assumptions;

	private long timeout = 1_000_000;

	public SharpSatSolver(Formula modelFormula) {
		final VariableMap variables = VariableMap.fromExpression(modelFormula);
		formula = new SharpSatSolverFormula(variables);
		modelFormula.getChildren().stream().map(c -> (Formula) c).forEach(formula::push);
		assumptions = new VariableAssignment(variables);
	}

	@Override
	public BigInteger countSolutions() {
		try {
			final CNF cnf = formula.getCNF();
			for (final Entry<Variable<?>, Object> entry : assumptions.getAllEntries()) {
				final int variable = entry.getKey().getIndex();
				cnf.addClause(new LiteralList((entry.getValue() == Boolean.TRUE) ? variable : -variable));
			}
			final Path tempFile = Files.createTempFile("sharpSATinput", ".dimacs");
			FileHandler.save(cnf, tempFile, new DIMACSFormatCNF());

			final List<String> command = new ArrayList<>(5);
			final String os = System.getProperty("os.name").toLowerCase().split("\\s+")[0];
			switch (os) {
			case "linux":
				command.add("./libs/sharpSAT");
				break;
			case "windows":
				command.add("libs\\sharpSAT.exe");
				break;
			default:
				Logger.logError("Unsupported operating system " + os);
				return INVALID_COUNT;
			}
			command.add("-noPP");
			command.add("-noCC");
			command.add("-noIBCP");
			command.add(tempFile.toString());

			final ProcessBuilder processBuilder = new ProcessBuilder(command);
			Process process = null;
			try {
				process = processBuilder.start();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				if (process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
					process = null;
					return reader.lines().findFirst().map(BigInteger::new).orElse(BigInteger.ZERO);
				} else {
					return INVALID_COUNT;
				}
			} finally {
				if (process != null) {
					process.destroyForcibly();
				}
			}
		} catch (final Exception e) {
			Logger.logError(e);
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
	public VariableMap getVariables() {
		return formula.getVariableMap();
	}

}
