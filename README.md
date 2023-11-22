# Streamline 🏊 

_THIS LANGUAGE IS IN DEVELOPMENT. THINGS MIGHT CHANGE OR BREAK UNTIL WE CUT A STABLE RELEASE_

A delightfully simple declarative, data driven programming language built for the EVM, specifically for developing and using Substreams in a more intuitive and powerful manner.

Check out the docs [HERE](doc/intro.md) to get started!

## Usage

#### NOTE The startup of the JVM is slow. I will release a standalone built for native hardware. But its not the end of the world.
#### TODO Bundle this into a standalone executable.

    $ lein run [PATH-TO-STRM-FILE]

## Examples
TODO

### Bugs / Features in progress

I'm sure there are lots of bugs waiting to be found. If you find something please let me know!

#### Features ready by 11/27/2023
1. Store Modules (This is coming super soon)
2. Arbitrary Functions
3. Modules w/ Param inputs
4. Helpers for bootstrapping and fetching abis for contracts. Think `streamline init`
5. Contract Instances
6. Sink Configs

#### Features coming soon
1. Arbitrary substeams spkg interop
2. Live REPL for substreams development, without wasting your data. (We will offer a free endpoint to query raw block data from)
3. LSP and Editor Support for things other than emacs, since I already built that :)

## License

Copyright © 2023 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
