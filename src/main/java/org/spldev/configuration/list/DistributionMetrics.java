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
package org.spldev.configuration.list;

import java.math.*;
import java.util.*;
import java.util.function.*;

import org.spldev.analysis.sharpsat.*;
import org.spldev.clauses.*;
import org.spldev.clauses.solutions.*;
import org.spldev.clauses.solutions.metrics.*;
import org.spldev.formula.*;
import org.spldev.formula.structure.*;
import org.spldev.formula.structure.atomic.literal.*;

public class DistributionMetrics extends AggregatableMetrics {

	public static class RatioDiffFunction {

		private final ModelRepresentation rep;
		private final VariableMap variableMap;
		private final BigDecimal totalCount;

		public RatioDiffFunction(ModelRepresentation rep) {
			this.rep = rep;
			variableMap = rep.getFormula().getVariableMap();
			final CountSolutionsAnalysis analysis = new CountSolutionsAnalysis();
			totalCount = analysis.getResult(rep).map(BigDecimal::new).orElseThrow();
		}

		public double compute(SolutionList sample, ClauseList expression) {
			final double sampleSize = sample.getSolutions().size();
			if (sampleSize == 0) {
				return 0;
			}
			int positiveCount = 0;
			for (final LiteralList solution : sample.getSolutions()) {
				for (final LiteralList clause : expression) {
					if (solution.containsAll(clause)) {
						positiveCount++;
						break;
					}
				}
			}
			final double sampleRatio = positiveCount / sampleSize;
			final CountSolutionsAnalysis analysis = new CountSolutionsAnalysis();
			final List<Formula> assumedConstraints = analysis.getAssumedConstraints();
			for (final LiteralList clause : expression) {
				assumedConstraints.add(Clauses.toOrClause(clause.negate(), variableMap));
			}
			final BigDecimal negativeCount = analysis.getResult(rep)
				.map(BigDecimal::new)
				.orElseThrow();
			final double actualRatio = 1 - negativeCount
				.divide(totalCount, MathContext.DECIMAL128)
				.doubleValue();
			return Math.abs(actualRatio - sampleRatio);
		}

	}

	private final RatioDiffFunction function;
	private final List<ClauseList> expressionList;
	private final String functionName;

	public DistributionMetrics(ModelRepresentation rep, List<ClauseList> expressionList, String functionName) {
		this.expressionList = expressionList;
		this.functionName = functionName;
		function = rep != null ? new RatioDiffFunction(rep) : null;
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
