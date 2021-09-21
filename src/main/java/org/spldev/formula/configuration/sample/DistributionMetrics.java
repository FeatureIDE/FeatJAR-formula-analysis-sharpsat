/* -----------------------------------------------------------------------------
 * Formula-Analysis Lib - Library to analyze propositional formulas.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Formula-Analysis Lib.
 * 
 * Formula-Analysis Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Formula-Analysis Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Formula-Analysis Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/formula-analysis> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.formula.configuration.sample;

import java.math.*;
import java.util.*;
import java.util.function.*;

import org.spldev.formula.*;
import org.spldev.formula.analysis.sharpsat.*;
import org.spldev.formula.clauses.*;
import org.spldev.formula.expression.*;
import org.spldev.formula.expression.atomic.literal.*;

public class DistributionMetrics extends AggregatableMetrics {

	private class DistributionFunction {

		private final ModelRepresentation rep;
		private final VariableMap variableMap;
		private final BigInteger totalCount;

		public DistributionFunction(ModelRepresentation rep) {
			this.rep = rep;
			variableMap = rep.getFormula().getVariableMap();
			final CountSolutionsAnalysis analysis = new CountSolutionsAnalysis();
			totalCount = analysis.getResult(rep).orElseThrow();
		}

		public double compute(SolutionList sample, ClauseList expression) {
			final double sampleSize = sample.getSolutions().size();
			if (sampleSize == 0) {
				return 0;
			}
			int positiveCount = 0;
			for (final LiteralList solution : sample.getSolutions()) {
				boolean covered = false;
				for (final LiteralList clause : expression) {
					if (solution.containsAll(clause)) {
						covered = true;
						break;
					}
				}
				if (covered) {
					positiveCount++;
				}
			}
			final double sampleRatio = positiveCount / sampleSize;
			final CountSolutionsAnalysis analysis = new CountSolutionsAnalysis();
			final List<Formula> assumedConstraints = analysis.getAssumedConstraints();
			for (final LiteralList clause : expression) {
				assumedConstraints.add(Clauses.toOrClause(clause.negate(), variableMap));
			}
			final double actualRatio = 1.0 / totalCount.divide(analysis.getResult(rep).orElseThrow()).doubleValue();

			return Math.abs(actualRatio - sampleRatio);
		}

	}

	private final DistributionFunction function;
	private final List<ClauseList> expressionList;
	private final String functionName;

	public DistributionMetrics(ModelRepresentation rep, List<ClauseList> expressionList, String functionName) {
		this.expressionList = expressionList;
		this.functionName = functionName;
		function = rep != null ? new DistributionFunction(rep) : null;
	}

	public static List<SampleMetric> getAllAggregates(ModelRepresentation rep,
		List<ClauseList> expressionList, String functionName) {
		return new DistributionMetrics(rep, expressionList, functionName).getAllAggregates();
	}

	@Override
	public SampleMetric getAggregate(String name, DoubleSupplier aggregate) {
		return new DoubleMetric(functionName + "_distribution_" + name, aggregate);
	}

	@Override
	public double[] computeValues() {
		final double[] values = new double[expressionList.size()];
		int index = 0;
		for (final ClauseList expression : expressionList) {
			values[index++] = function.compute(sample, expression);
		}
		return values;
	}

}
