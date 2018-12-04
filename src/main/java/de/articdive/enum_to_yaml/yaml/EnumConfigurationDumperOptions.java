/*
 * Copyright (C) 2018 Lukas Mansour
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.articdive.enum_to_yaml.yaml;

import org.yaml.snakeyaml.DumperOptions;

public class EnumConfigurationDumperOptions extends DumperOptions {
    private char separatorChar;

    public EnumConfigurationDumperOptions() {
        setWidth(10000);
        setPrettyFlow(true);
        setDefaultFlowStyle(FlowStyle.BLOCK);
        setDefaultScalarStyle(ScalarStyle.PLAIN);
        setLineBreak(LineBreak.getPlatformLineBreak());
        setSeparatorChar('.');
    }

    public char getSeparatorChar() {
        return separatorChar;
    }

    public void setSeparatorChar(char separatorChar) {
        this.separatorChar = separatorChar;
    }
}
