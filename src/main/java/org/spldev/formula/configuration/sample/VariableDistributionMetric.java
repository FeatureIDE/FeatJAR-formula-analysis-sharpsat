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
 * Tests whether a set of configurations achieves t-wise feature coverage.
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
		final VariableMap variables = sample.getVariables();
		for (int i = 1; i <= variables.getMaxIndex(); i++) {
			int count = 0;
			for (final LiteralList solution : sample.getSolutions()) {
				if (solution.containsAnyLiteral(i)) {
					count++;
				}
			}
			final double ratio = (double) count / sample.getSolutions().size();
		}
		final CountSolutionsAnalysis analysis = new CountSolutionsAnalysis();
		final BigInteger result = analysis.getResult(rep).orElseThrow();
		final BigInteger size = BigInteger.valueOf(sample.getSolutions().size());
		size.divide(result);
		return 1;
	}

	@Override
	public String getName() {
		return "Completeness";
	}

}
