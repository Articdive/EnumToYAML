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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnumConfigurationBuilder {
    private File file;
    private List<ConfigurationEnum> configurationEnums;
    private EnumConfigurationDumperOptions dumperOptions = new EnumConfigurationDumperOptions();

    public <T extends Enum<T> & ConfigurationEnum> EnumConfigurationBuilder(File file, Class<T> configurationEnumClass) {
        this.file = file;
        this.configurationEnums = new ArrayList<>(Arrays.asList(configurationEnumClass.getEnumConstants()));
    }

    EnumConfigurationBuilder(File file, List<ConfigurationEnum> configurationEnums, EnumConfigurationDumperOptions dumperOptions) {
        this.file = file;
        this.configurationEnums = configurationEnums;
        this.dumperOptions = dumperOptions;
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
     * What {@link Character} should be used for programmatic node separation?
     *
     * @param separatorCharacter {@link Character}
     * @return this {@link EnumConfigurationBuilder}
     */
    public EnumConfigurationBuilder setSeparatorCharacter(char separatorCharacter) {
        dumperOptions.setSeparatorChar(separatorCharacter);
        return this;
    }

    /**
     * Adds ConfigurationEnumerations from other sources.
     *
     * @param enumsToAdd {@link ConfigurationEnum} to add
     * @return this {@link EnumConfigurationBuilder}
     */
    public EnumConfigurationBuilder addConfigurationEnumeration(ConfigurationEnum... enumsToAdd) {
        for (ConfigurationEnum enumToAdd : enumsToAdd) {
            for (ConfigurationEnum configurationEnum : configurationEnums) {
                if (enumToAdd.equals(configurationEnum)) {
                    throw new IllegalArgumentException("You cannot add identical ConfigurationEnums!");
                } else if (enumToAdd.getPath().equalsIgnoreCase(configurationEnum.getPath())) {
                    throw new IllegalArgumentException("You cannot add two ConfigurationEnums with the same path!");
                } else {
                    configurationEnums.add(enumToAdd);
                }
            }
        }
        return this;
    }

    /**
     * Builds the {@link EnumConfiguration} from this {@link EnumConfigurationBuilder}.
     *
     * @return new {@link EnumConfiguration}
     */
    public EnumConfiguration build() {
        return new EnumConfiguration(file, configurationEnums, dumperOptions);
    }
}
