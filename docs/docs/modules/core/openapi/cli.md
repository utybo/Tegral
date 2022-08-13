---
sidebar_position: 3
---

# CLI

The Tegral OpenAPI CLI provides an easy and convenient way of converting [OpenAPI scripts](./scripting.md) to proper OpenAPI JSON and YAML files.

## Running the CLI

You can use [JBang](https://jbang.dev) to run the CLI without installing it separately. Use the following:

```bash
jbang guru.zoroark.tegral:tegral-openapi-cli:VERSION --help
```

Replace `VERSION` by the version you want.

:::note

If you want to run snapshot versions, you'll need an extra parameter:

```bash
jbang run --repos https://s01.oss.sonatype.org/content/repositories/snapshots/ guru.zoroark.tegral:tegral-openapi-cli:VERSION --help
```

:::

## Options

The following options are available:

| Short flag | Long flag | Description |
|:----------:|:---------:| ----------- |
| `-o` | `--output` | Output file. If present, the result will be written to this file instead of being printed on the standard output. |
| `-f` | `--formation` | Choose the format for the output, `json` (default) or `yaml` |
| `-q` | `--quiet` | Suppress all non-error output, except for the result, i.e. suppresses log messages. |
| `-a` | `--openapi-version` | Choose the output version for the OpenAPI file, `3.0` (default) or `3.1` |
| `-h` | `--help` | Prints a help message |
