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
		final BigDecimal totalSize = new CountSolutionsAnalysis() //
			.getResult(rep) //
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
