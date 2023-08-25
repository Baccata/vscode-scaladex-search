# Scaladex search

This humble vscode plugin adds a "Scaladex search" action that lets you search
for Scala libraries and store them in your clipboard, ready to paste in (depending on which file is open):

* SBT build definitions
* mill build definitions
* bleep build definitions
* ammonite scripts
* scala-cli scripts
* scala files

![demo](https://media.githubusercontent.com/media/Baccata/vscode-scaladex-search/main/assets/scaladex.gif)


## Installation

Install the extension from the [Marketplace](https://marketplace.visualstudio.com/items?itemName=baccata.scaladex-search)

## Instructions

1. Open the command palette
2. Select "Scaladex search"
3. type the name of a Scala library and press enter
4. Select the library you're looking for
5. Select one or more artifacts
6. Select an artifact version
7. CTRL-V wherever

## Development

The plugin is written in Scalajs.

To run VSCode in an Extension Development Host (useful for manual testing), run `sbt open`.

See [pme123/vscode-scalajs-hello](https://github.com/pme123/vscode-scalajs-hello) for more developent instructions.

Refs:

* [accessible-scala](https://marketplace.visualstudio.com/items?itemName=scala-center.accessible-scala)
* [helloworld-minimal-sample](https://github.com/Microsoft/vscode-extension-samples/tree/master/helloworld-minimal-sample)
* [helloworld-scalajs-sample](https://github.com/pme123/vscode-scalajs-hello)
* [Scalably Typed](https://github.com/oyvindberg/ScalablyTyped)
* [SBT](https://www.scala-sbt.org)
* [ScalaJS](http://www.scala-js.org)
* [scalajs-bundler](https://github.com/scalacenter/scalajs-bundler)

## Packaging / publishing

You need a local install of NodeJS with NPM. Install VSCode extension manager with:

```bash
npm install -g @vscode/vsce
```

Then use:

```bash
sbt fullOptJS
vsce package
vsce publish
```
