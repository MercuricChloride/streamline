# Structure of this codebase

This codebase is pretty simple. I am going to be adding a number of features onto it, but for now it just hosts a simple compiler.

## Compilation Flow

The whole compilation flow is as follows:

1. We parse the source code to create a parse-tree
   Because the parse tree doesn't contain that much extra data, we only operate on this single data structure for the whole compiler.
   The grammar for the parser, and the parser itself is defined in `src/streamline/ast/parser.clj`

2. We make a couple passes over the parse-tree, gathering and annotating the nodes' metadata with information relevant to compilation.
   This includes things such as types, protobuf messages, and inputs. These annotations are later used for the actual code generation.
   All of this logic is stored in `src/streamline/ast/metadata.clj`

3. After we annotate the code, we simply traverse the tree and generate code based on the structure and metadata.
   The templating logic is stored in `src/streamline/templating/*`
   And the templates are stored in `resources/templates/`

## REPL
TODO

## LSP
TODO 

## Editor Extensions
TODO
