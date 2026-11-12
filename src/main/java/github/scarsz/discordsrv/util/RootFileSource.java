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

package github.scarsz.discordsrv.util;

import github.scarsz.configuralize.DynamicConfig;
import github.scarsz.configuralize.Language;
import github.scarsz.configuralize.Source;

import java.io.File;
import java.net.URL;

/**
 * A {@link Source} that always loads a single file from the plugin jar instead of a language-specific file.
 */
public class RootFileSource extends Source {

    private final String resourcePath;

    public RootFileSource(DynamicConfig config, Class<?> clazz, String resourceName, String resourcePath, File file) {
        super(config, clazz, resourceName, file);
        this.resourcePath = resourcePath;
    }

    @Override
    public String getResourcePath() {
        return resourcePath;
    }

    @Override
    public String getResourcePath(Language language) {
        return resourcePath;
    }

    @Override
    public URL getResource() {
        return getClazz().getResource(resourcePath);
    }

    @Override
    public URL getResource(Language language) {
        return getClazz().getResource(resourcePath);
    }

    @Override
    public boolean isLanguageAvailable() {
        return true;
    }

    @Override
    public boolean isLanguageAvailable(Language language) {
        return true;
    }

}
