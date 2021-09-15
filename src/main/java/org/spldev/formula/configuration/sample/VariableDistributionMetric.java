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
package org.spldev.formula.configuration.sample;

import java.math.*;

import org.spldev.formula.*;
import org.spldev.formula.analysis.sharpsat.*;
import org.spldev.formula.clauses.*;
import org.spldev.formula.expression.atomic.literal.*;

/**
 * Tests whether a set of configurations achieves the same variable distribution
 * as the complete valid configuration space.
 *
 * @author Sebastian Krieter
 */
public class VariableDistributionMetric implements SampleMetric {

	private ModelRepresentation rep;

	public VariableDistributionMetric(ModelRepresentation rep) {
		this.rep = rep;
	}

	@Override
	public double get(SolutionList sample) {
		if (sample.getSolutions().isEmpty()) {
			return 0;
		}
		CountSolutionsAnalysis analysis = new CountSolutionsAnalysis();
		final BigInteger totalCount = analysis.getResult(rep).orElseThrow();

		final VariableMap variables = sample.getVariables();
		final double sampleSize = sample.getSolutions().size();
		int diffSum = 0;
		for (int i = 1; i <= variables.getMaxIndex(); i++) {
			int positiveCount = 0;
			for (final LiteralList solution : sample.getSolutions()) {
				if (solution.containsAnyLiteral(i)) {
					positiveCount++;
				}
			}
			final double sampleRatio = positiveCount / sampleSize;

			analysis = new CountSolutionsAnalysis();
			analysis.getAssumptions().set(i, true);
			final double actualRatio = 1.0 / totalCount.divide(analysis.getResult(rep).orElseThrow()).doubleValue();

			diffSum += Math.abs(actualRatio - sampleRatio);
		}
		return (double) diffSum / variables.getMaxIndex();
	}

	@Override
	public String getName() {
		return "DistributionPrecision";
	}

}
