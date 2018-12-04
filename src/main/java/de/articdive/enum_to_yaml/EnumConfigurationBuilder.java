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

package de.articdive.enum_to_yaml;

import de.articdive.enum_to_yaml.interfaces.ConfigurationEnum;
import de.articdive.enum_to_yaml.yaml.EnumConfigurationDumperOptions;
import org.yaml.snakeyaml.DumperOptions.LineBreak;

import java.io.File;

public class EnumConfigurationBuilder {
    private File file;
    private ConfigurationEnum[] configurationEnums;
    private EnumConfigurationDumperOptions dumperOptions = new EnumConfigurationDumperOptions();

    public <T extends Enum<T> & ConfigurationEnum> EnumConfigurationBuilder(File file, Class<T> configurationEnumClass) {
        this.file = file;
        this.configurationEnums = configurationEnumClass.getEnumConstants();
    }

    /**
     * Sets the optimal width for the new Configuration.
     *
     * @param width {@link Integer}
     * @return this {@link EnumConfigurationBuilder}
     */
    public EnumConfigurationBuilder setWidth(int width) {
        dumperOptions.setWidth(width);
        return this;
    }

    /**
     * Sets the indentation for the new configuration.
     *
     * @param indentation {@link Integer}
     * @return this {@link EnumConfigurationBuilder}
     */
    public EnumConfigurationBuilder setIndentation(int indentation) {
        if (indentation < 2 || indentation > 9) {
            throw new IllegalArgumentException("Indentation cannot be smaller than 2 or larger than 9");
        }
        dumperOptions.setIndent(indentation);
        return this;
    }


    /**
     * Sets the indicator indetation for the new configuration.
     *
     * @param indicatorIndentation {@link Integer}
     * @return this {@link EnumConfigurationBuilder}
     */
    public EnumConfigurationBuilder setIndicatorIndentation(int indicatorIndentation) {
        dumperOptions.setIndicatorIndent(indicatorIndentation);
        return this;
    }

    /**
     * What {@link LineBreak} should the configuration have.
     *
     * @param lineBreak {@link LineBreak}
     * @return this {@link EnumConfigurationBuilder}
     */
    public EnumConfigurationBuilder setLineBreak(LineBreak lineBreak) {
        dumperOptions.setLineBreak(lineBreak);
        return this;
    }

    /**
     * What {@link Character} should be used for in-java node separation?
     *
     * @param separatorCharacter {@link Character}
     * @return this {@link EnumConfigurationBuilder}
     */
    public EnumConfigurationBuilder setSeparatorCharacter(char separatorCharacter) {
        dumperOptions.setSeparatorChar(separatorCharacter);
        return this;
    }

    /**
     * Builds the {@link EnumConfiguration} from this {@link EnumConfigurationBuilder}.
     *
     * @return new EnumConfiguration {@link EnumConfiguration}
     */
    public EnumConfiguration build() {
        return new EnumConfiguration(file, configurationEnums, dumperOptions);
    }
}
