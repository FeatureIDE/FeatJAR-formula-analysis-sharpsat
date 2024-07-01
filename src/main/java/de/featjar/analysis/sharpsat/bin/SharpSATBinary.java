/*
 * Copyright (C) 2024 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-bin-sharpsat.
 *
 * bin-sharpsat is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * bin-sharpsat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bin-sharpsat. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-bin-sharpsat> for further information.
 */
package de.featjar.analysis.sharpsat.bin;

import de.featjar.base.data.Sets;
import de.featjar.base.env.ABinary;
import de.featjar.base.env.HostEnvironment;
import java.io.IOException;
import java.util.LinkedHashSet;

public class SharpSATBinary extends ABinary {
    public SharpSATBinary() throws IOException {}

    @Override
    public String getExecutableName() {
        return HostEnvironment.isWindows() ? "sharpSAT.exe" : "sharpSAT";
    }

    @Override
    public LinkedHashSet<String> getResourceNames() {
        return HostEnvironment.isWindows() ? Sets.of("sharpSAT.exe", "gmp-10.dll") : Sets.of("sharpSAT");
    }
}
