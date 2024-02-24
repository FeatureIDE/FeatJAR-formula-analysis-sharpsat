/*
 * Copyright (C) 2024 FeatJAR-Development-Team
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
package de.featjar.formula.configuration.list;

import de.featjar.base.computation.IComputation;
import de.featjar.formula.analysis.bool.BooleanSolutionList;
import de.featjar.formula.analysis.metrics.ISampleMetric;
import de.featjar.formula.analysis.sharpsat.ComputeSolutionCountSharpSAT;
import de.featjar.formula.structure.formula.IFormula;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Computes the ratio of configuration space covered by a configuration sample.
 *
 * @author Sebastian Krieter
 */
// TODO Make computation
public class CompletenessMetric implements ISampleMetric {

    private IComputation<IFormula> rep;

    public CompletenessMetric(IComputation<IFormula> rep) {
        this.rep = rep;
    }

    @Override
    public double get(BooleanSolutionList sample) {
        final BigDecimal totalSize = rep //
                .map(ComputeSolutionCountSharpSAT::new) //
                .computeResult()
                .map(BigDecimal::new) //
                .orElseThrow();
        return totalSize.signum() > 0 //
                ? new BigDecimal(sample.size()) //
                        .divide(totalSize, MathContext.DECIMAL128) //
                        .doubleValue()
                : 0;
    }

    @Override
    public String getName() {
        return "Completeness";
    }
}
