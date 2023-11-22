# Why did we build this?

Streamline was not born on a whim. I didn't just wake up and decide that I would build a language. Instead the concepts and ideas behind Streamline have existed for a while, even before I knew exactly what it was.

I work with blockchain data every day. I have used just about every tool available for working with and analyzing on chain data. There are good ideas and cool features in a lot of the tools. But I have never worked with one tool that feels truly "complete".

It seems there are two kinds of tools for working with blockchain data, Bicycles and Airplanes.

## Bicycles
These are tools or APIs that allow you to do simple things, simply!
Examples include: Alchemy or Etherscan.
They allow you to view transactions on an account or events emitted from a contract.

These are great for simple things like I mentioned above, but they are very awkward to use outside of their intended scope. Much like a bicycle.

A bicycle is simple to operate and inexpensive. You can learn how to ride a bike in a couple of hours. And if you need to go short distances, they are very convenient. However if you need to travel extremely long distances, you are in for a bad time.

These tools are the much the same. They require minimal time to learn and might have a cheap monetary price. But they are scope limited and going beyond their intended use is painful.

## Airplanes
These are tools or frameworks which are more geared for "power users". Powerful tools that allow you to solve complex problems.
Examples include: Dune Analytics, Subgraphs, and Substreams.

Do you need to store a lot of data? Sure.
Do you need some complex aggregation of data? Sure.

These kinds of tools shine with complex tasks as they make the task much more practical than their simpler counterparts

However my general experience with these tools is that they are technically powerful. They offer little to no guardrails or tools to prevent silly mistakes and tame complexity when a project grows.

This leads to cases where a developer may be writing syntaxically valid code, that later crashes at runtime after syncing for 10 minutes or worse, silently introduces hard to find bugs in your data. All because they forgot about an interaction a block of code had in an unrelated file.

Additionally these tools are often not practical choices for simple tasks, as they often require loads of configuration and upfront building in order to even get anything useful. Much like an airplane.

An airplane is a fantastic tool for traveling long distances. But using an airplane is hard and dangerous, you must take lots of time to learn how to safely fly an airplane, or pay a pilot who has put in lots of time into learning how to safely fly. This mirrors the learning curve and usage of most of these tools. And if you want to go to the grocery store down the street, it doesn't really make much sense to taxi an airplane even if you may technically be able to do so.

## An Alternative

Streamline looks to offer a solution to a lot of these problems and make the development process much more enjoyable and powerful.

How do we accomplish this? Lets continue back in the intro [here](./intro.md).
