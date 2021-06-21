# Darwin Core

If you're reading this, it's just a weird experiment at this point.
We're experimenting with using Compose to render a native UI with UIKit. 
We're starting with some of the common widget definitions in `web/widgets`, 
although we'll likely differ pretty soon, as they seem focused more on web.

## Why?

This isn't a port of Compose UI for native, so it's not "Compose for iOS". The idea is 
for something that covers simpler cases but is easy to extend as needed. 
We'll see how it goes.

## Can you use this?

Technically, maybe, but it barely qualifies as an experiment at this point.

## Install

We're using somewhat custome compiler plugins and runtime. They're currently not
included in source form. You'll need to unzip `repository.zip` and put the 
folders into `~/.m2/repository`.

In intellij, open the `web` folder. After it syncs, open the gradle explorer,
find `darwin-example > compose desktop > run`. This will run the compose 
desktop version of the sample.

To run iOS, open `web/darwin-example/DarwinExample` in a terminal, 
and run `pod install`. When complete, open `DarwinExample.xcworkspace` in Xcode.
Select a reasonable simulator (iPhone 12 or similar), then run the sample.