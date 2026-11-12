/*
 * DiscordSRV - https://github.com/DiscordSRV/DiscordSRV
 *
 * Copyright (C) 2016 - 2024 Austin "Scarsz" Shapiro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

package github.scarsz.discordsrv.test;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AllConfigsHaveKeysTest {

    private final File resources = new File("src/main/resources");
    private final Yaml yaml = new Yaml();

    @Test
    public void test() throws IOException {
        List<String> configs = Arrays.asList("config.yml", "messages.yml", "synchronization.yml");
        for (String config : configs) {
            File file = new File(resources, config);
            Map<String, Object> values = yaml.loadAs(FileUtils.readFileToString(file, "UTF-8"), Map.class);
            if (values == null) {
                throw new IOException("Failed to load " + config);
            }
        }
    }

}
