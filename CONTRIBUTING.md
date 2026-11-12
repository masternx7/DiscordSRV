# Reporting an issue
Do ***not*** report a problem without going through the following checklist first.

> Did you reload the plugin?

If you did, we won't help/support you. DiscordSRV doesn't like to be reloaded.
It doesn't matter if you reloaded it through PlugMan. It still breaks it. Restart your server completely.

> Are you using the most up-to-date version?

If you aren't, we can't help you. It's as simple as that. Update your plugin.

> Have you turned on debug mode?

This is referring to the `Debug` config option. Before reporting an issue, add `all` to `Debug`,
`/discord reload`, then re-do what is causing your problem. Without debug information in your log, we can't
diagnose what's wrong and won't be able to help as good as we'd normally be able to.

> Have you attached the output of `/discord debug`?

This is our way of getting all the required information to assist you effectively and to help diagnose errors.
Without this, we can't help.

# Translating
The config, messages and synchronization files are single files at the root of `src/main/resources`.
Translate the values in those files directly. Internal messages can be translated in
`src/main/java/github/scarsz/discordsrv/util/LangUtil.java`.

# Submitting a pull request
Submit changes targeting the `develop` branch. PR's to `master` will be denied. Try your best to follow the
existing coding practices. Config keys are lower-case and use hyphens, grouped under a section.
Use lombok `@Getter`'s/`@Setter`'s where applicable.
