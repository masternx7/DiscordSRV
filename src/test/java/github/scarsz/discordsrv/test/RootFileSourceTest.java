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

import github.scarsz.configuralize.DynamicConfig;
import github.scarsz.configuralize.Language;
import github.scarsz.discordsrv.util.RootFileSource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RootFileSourceTest {

    @Test
    public void test() throws Exception {
        File directory = Files.createTempDirectory("discordsrv").toFile();

        DynamicConfig config = new DynamicConfig();
        config.addSource(new RootFileSource(config, RootFileSourceTest.class, "config", "/config.yml", new File(directory, "config.yml")));
        config.addSource(new RootFileSource(config, RootFileSourceTest.class, "messages", "/messages.yml", new File(directory, "messages.yml")));
        config.addSource(new RootFileSource(config, RootFileSourceTest.class, "synchronization", "/synchronization.yml", new File(directory, "synchronization.yml")));
        config.setLanguage(Language.EN);
        config.saveAllDefaults();
        config.loadAll();

        assertEquals("BOTTOKEN", config.getString("bot.token"));
        assertEquals("000000000000000000", config.getString("channels.console-id"));
        assertEquals(3, config.getInt("nickname-synchronization.cycle-time"));
        assertEquals(true, config.getBoolean("group-role-synchronization.minecraft-is-authoritative"));
    }

}
