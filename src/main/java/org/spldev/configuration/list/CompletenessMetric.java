/* -----------------------------------------------------------------------------
 * formula-analysis-sharpsat - Analysis of propositional formulas using sharpSAT
 * Copyright (C) 2021 Sebastian Krieter
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
 * See <https://github.com/FeatJAR/formula-analysis-sharpsat> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.configuration.list;

import java.math.*;

import org.spldev.analysis.sharpsat.*;
import org.spldev.clauses.solutions.*;
import org.spldev.clauses.solutions.metrics.*;
import org.spldev.formula.*;

/**
 * Computes the ratio of configuration space covered by a configuration sample.
 *
 * @author Sebastian Krieter
 */
public class CompletenessMetric implements SampleMetric {

	private ModelRepresentation rep;

	public CompletenessMetric(ModelRepresentation rep) {
		this.rep = rep;
	}

	@Override
	public double get(SolutionList sample) {
		final BigDecimal totalSize = rep //
			.getResult(new CountSolutionsAnalysis()) //
			.map(BigDecimal::new) //
			.orElseThrow();
		return totalSize.signum() > 0 //
			? new BigDecimal(sample.getSolutions().size()) //
				.divide(totalSize, MathContext.DECIMAL128) //
				.doubleValue()
			: 0;
	}

	@Override
	public String getName() {
		return "Completeness";
	}

}
